/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.commons.compress.utils.CharsetNames;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingOutputStream;

public class PayloadRecorder implements AutoCloseable, PayloadProvider
{
    public static class Result
    {
        private final long size;

        private final byte[] digest;

        private Result ( final long size, final byte[] digest )
        {
            this.size = size;
            this.digest = digest;
        }

        public long getSize ()
        {
            return this.size;
        }

        public byte[] getDigest ()
        {
            return this.digest;
        }
    }

    private final boolean autoFinish;

    private final Path tempFile;

    private final CountingOutputStream payloadCounter;

    private final CountingOutputStream archiveCounter;

    private final CpioArchiveOutputStream archiveStream;

    private OutputStream fileStream;

    private boolean finished;

    private boolean closed;

    private PayloadCoding payloadCoding;

    private Optional<String> payloadFlags;

    private DigestAlgorithm fileDigestAlgorithm;

    public PayloadRecorder () throws IOException
    {
        this ( true, PayloadCodingRegistry.get ( PayloadCodingRegistry.GZIP ), null, DigestAlgorithm.MD5  );
    }

    public PayloadRecorder ( final boolean autoFinish ) throws IOException
    {
        this ( autoFinish, PayloadCodingRegistry.get ( PayloadCodingRegistry.GZIP ), null, DigestAlgorithm.MD5 );
    }

    public PayloadRecorder ( final boolean autoFinish, final PayloadCoding payloadCoding, final String payloadFlags, final DigestAlgorithm fileDigestAlgorithm ) throws IOException
    {
        this.autoFinish = autoFinish;

        this.fileDigestAlgorithm = fileDigestAlgorithm;

        this.tempFile = Files.createTempFile ( "rpm-", null );

        try
        {
            this.fileStream = new BufferedOutputStream ( Files.newOutputStream ( this.tempFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING ) );

            this.payloadCounter = new CountingOutputStream ( this.fileStream );

            this.payloadCoding = payloadCoding;

            this.payloadFlags = Optional.ofNullable ( payloadFlags );

            final OutputStream payloadStream = this.payloadCoding.createOutputStream ( this.payloadCounter, this.payloadFlags );

            this.archiveCounter = new CountingOutputStream ( payloadStream );

            // setup archive stream

            this.archiveStream = new CpioArchiveOutputStream ( this.archiveCounter, CpioConstants.FORMAT_NEW, 4, CharsetNames.UTF_8 );
        }
        catch ( final IOException e )
        {
            Files.deleteIfExists ( this.tempFile );
            throw e;
        }
    }

    public Result addFile ( final String targetPath, final Path path ) throws IOException
    {
        return addFile ( targetPath, path, null );
    }

    public Result addFile ( final String targetPath, final Path path, final Consumer<CpioArchiveEntry> customizer ) throws IOException
    {
        final long size = Files.size ( path );

        final CpioArchiveEntry entry = new CpioArchiveEntry ( CpioConstants.FORMAT_NEW, targetPath );
        entry.setSize ( size );

        if ( customizer != null )
        {
            customizer.accept ( entry );
        }

        this.archiveStream.putArchiveEntry ( entry );

        MessageDigest digest;
        try
        {
            digest = createDigest ();
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IOException ( e );
        }

        try ( InputStream in = new BufferedInputStream ( Files.newInputStream ( path ) ) )
        {
            ByteStreams.copy ( new DigestInputStream ( in, digest ), this.archiveStream );
        }

        this.archiveStream.closeArchiveEntry ();

        return new Result ( size, digest.digest () );
    }

    public Result addFile ( final String targetPath, final ByteBuffer data ) throws IOException
    {
        return addFile ( targetPath, data, null );
    }

    public Result addFile ( final String targetPath, final ByteBuffer data, final Consumer<CpioArchiveEntry> customizer ) throws IOException
    {
        final long size = data.remaining ();

        final CpioArchiveEntry entry = new CpioArchiveEntry ( CpioConstants.FORMAT_NEW, targetPath );
        entry.setSize ( size );

        if ( customizer != null )
        {
            customizer.accept ( entry );
        }

        this.archiveStream.putArchiveEntry ( entry );

        // record digest

        MessageDigest digest;
        try
        {
            digest = createDigest ();
            digest.update ( data.slice () );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IOException ( e );
        }

        // write data

        final WritableByteChannel channel = Channels.newChannel ( this.archiveStream );
        while ( data.hasRemaining () )
        {
            channel.write ( data );
        }

        // close archive entry

        this.archiveStream.closeArchiveEntry ();

        return new Result ( size, digest.digest () );
    }

    private MessageDigest createDigest () throws NoSuchAlgorithmException
    {
        return MessageDigest.getInstance ( this.fileDigestAlgorithm.getAlgorithm () );
    }

    public Result addFile ( final String targetPath, final InputStream stream ) throws IOException
    {
        return addFile ( targetPath, stream, null );
    }

    public Result addFile ( final String targetPath, final InputStream stream, final Consumer<CpioArchiveEntry> customizer ) throws IOException
    {
        final Path tmpFile = Files.createTempFile ( "rpm-payload-", null );
        try
        {
            try ( OutputStream os = Files.newOutputStream ( tmpFile ) )
            {
                ByteStreams.copy ( stream, os );
            }

            return addFile ( targetPath, tmpFile, customizer );
        }
        finally
        {
            Files.deleteIfExists ( tmpFile );
        }
    }

    public Result addDirectory ( final String targetPath, final Consumer<CpioArchiveEntry> customizer ) throws IOException
    {
        final CpioArchiveEntry entry = new CpioArchiveEntry ( CpioConstants.FORMAT_NEW, targetPath );

        if ( customizer != null )
        {
            customizer.accept ( entry );
        }

        this.archiveStream.putArchiveEntry ( entry );
        this.archiveStream.closeArchiveEntry ();

        return new Result ( 4096, null );
    }

    public Result addSymbolicLink ( final String targetPath, final String linkTo, final Consumer<CpioArchiveEntry> customizer ) throws IOException
    {
        final byte[] bytes = linkTo.getBytes ( StandardCharsets.UTF_8 );

        final CpioArchiveEntry entry = new CpioArchiveEntry ( CpioConstants.FORMAT_NEW, targetPath );
        entry.setSize ( bytes.length );

        if ( customizer != null )
        {
            customizer.accept ( entry );
        }

        this.archiveStream.putArchiveEntry ( entry );
        this.archiveStream.write ( bytes );
        this.archiveStream.closeArchiveEntry ();

        return new Result ( bytes.length, null );
    }

    /**
     * Stop recording payload data
     * <p>
     * If the recorder is already finished then nothing will happen
     * </p>
     *
     * @throws IOException
     *             in case of IO errors
     */
    public void finish () throws IOException
    {
        if ( this.finished )
        {
            return;
        }

        this.finished = true;

        this.archiveStream.close ();
    }

    @Override
    public long getArchiveSize () throws IOException
    {
        checkFinished ( true );

        return this.archiveCounter.getCount ();
    }

    @Override
    public long getPayloadSize () throws IOException
    {
        checkFinished ( true );

        return this.payloadCounter.getCount ();
    }

    @Override
    public PayloadCoding getPayloadCoding ()
    {
        return this.payloadCoding;
    }

    @Override
    public Optional<String> getPayloadFlags ()
    {
        return this.payloadFlags;
    }

    @Override
    public DigestAlgorithm getFileDigestAlgorithm ()
    {
        return this.fileDigestAlgorithm;
    }

    @Override
    public FileChannel openChannel () throws IOException
    {
        checkFinished ( true );

        return FileChannel.open ( this.tempFile, StandardOpenOption.READ );
    }

    private void checkFinished ( final boolean allowAutoFinish ) throws IOException
    {
        if ( !this.finished && this.autoFinish && allowAutoFinish )
        {
            finish ();
        }

        if ( !this.finished )
        {
            throw new IllegalStateException ( "Recoderd has to be finished before accessing payload information or data" );
        }
        if ( this.closed )
        {
            throw new IllegalStateException ( "Recorder is already closed" );
        }
    }

    @Override
    public void close () throws IOException
    {
        this.closed = true;

        try
        {
            // simply close the file stream

            if ( this.fileStream != null )
            {
                this.fileStream.close ();
            }
        }
        finally
        {
            // and delete the temp file

            Files.deleteIfExists ( this.tempFile );
        }
    }
}
