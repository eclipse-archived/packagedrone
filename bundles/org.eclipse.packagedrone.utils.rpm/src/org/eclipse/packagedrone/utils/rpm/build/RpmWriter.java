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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.eclipse.packagedrone.utils.rpm.header.Header;
import org.eclipse.packagedrone.utils.rpm.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * A low level RPM file writer
 * <p>
 * This class handles constructing RPM files. It does not really care about the
 * contents it writes. Still the content and the format of the content is
 * important, but this is taken care of by the {@link RpmBuilder}.
 * </p>
 *
 * @author Jens Reimann
 */
public class RpmWriter implements AutoCloseable
{
    private final static Logger logger = LoggerFactory.getLogger ( RpmWriter.class );

    private final FileChannel file;

    private final RpmLead lead;

    private final ByteBuffer header;

    private boolean finished;

    private PayloadProvider payloadProvider;

    public RpmWriter ( final Path path, final String name, final Header<RpmTag> header ) throws IOException
    {
        this ( path, name, header, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE );
    }

    public RpmWriter ( final Path path, final String name, final Header<RpmTag> header, final OpenOption... options ) throws IOException
    {
        this ( path, new RpmLead ( (byte)3, (byte)0, name, 5 ), header, options );
    }

    RpmWriter ( final Path path, final RpmLead lead, final Header<RpmTag> header, final OpenOption... options ) throws IOException
    {
        this.file = FileChannel.open ( path, options );
        this.lead = lead;

        this.header = Headers.render ( header.makeEntries (), true, Rpms.IMMUTABLE_TAG_HEADER );
    }

    public void setPayload ( final PayloadProvider payloadProvider ) throws IOException
    {
        checkNotFinished ();

        Objects.requireNonNull ( payloadProvider );

        this.payloadProvider = payloadProvider;
    }

    private void checkNotFinished ()
    {
        if ( this.finished )
        {
            throw new IllegalStateException ( "Writing of RPM is already finished" );
        }
    }

    private void debug ( final String fmt, final Object... args )
    {
        logger.debug ( String.format ( fmt, args ) );
    }

    private void writeLead () throws IOException
    {
        // write lead

        final ByteBuffer lead = ByteBuffer.allocate ( Rpms.LEAD_MAGIC.length + 2 + 4 + 66 + 2 + 2 + 16 );

        lead.put ( Rpms.LEAD_MAGIC );
        lead.put ( this.lead.getMajor () );
        lead.put ( this.lead.getMinor () );

        // TODO: 4 bytes OS + ARCH

        lead.putInt ( 0 );

        // write package name

        {
            final ByteBuffer nameData = StandardCharsets.UTF_8.encode ( this.lead.getName () );
            final int len = nameData.remaining ();
            if ( len > 66 )
            {
                throw new IOException ( "Name exceeds 66 bytes" );
            }

            lead.put ( nameData );
            if ( len < 66 )
            {
                // fill up
                lead.put ( Rpms.EMPTY_128, 0, 66 - len );
            }
        }

        // TODO: 2 bytes OS

        lead.putShort ( (short)0 );

        // 2 bytes signature

        lead.putShort ( (short)this.lead.getSignatureVersion () );

        // 16 bytes reserved

        lead.put ( Rpms.EMPTY_128, 0, 16 );

        lead.flip ();
        safeWrite ( lead );
    }

    private void safeWrite ( final ByteBuffer data ) throws IOException
    {
        while ( data.hasRemaining () )
        {
            this.file.write ( data );
        }
    }

    private void writeSignatureHeader ( final Header<?> header ) throws IOException
    {
        // render header

        final ByteBuffer buffer = Headers.render ( header.makeEntries (), false, Rpms.IMMUTABLE_TAG_SIGNATURE );

        final int payloadSize = buffer.remaining ();

        // header

        debug ( "start header - offset: %s, len: %s%n", this.file.position (), payloadSize );
        safeWrite ( buffer );

        // padding

        final int padding = payloadSize % 8;

        if ( padding > 0 )
        {
            safeWrite ( ByteBuffer.wrap ( Rpms.EMPTY_128, 0, padding ) );
            debug ( "write - padding - %s%n", padding );
        }
    }

    @Override
    public void close () throws IOException
    {
        try
        {
            finish ();
        }
        finally
        {
            this.file.close ();
        }
    }

    private void finish () throws IOException
    {
        if ( this.finished )
        {
            return;
        }

        if ( this.payloadProvider == null )
        {
            throw new IOException ( "Unable to finish RPM file, payload provider not set" );
        }

        this.finished = true;

        final int headerSize = this.header.remaining ();
        final long payloadSize = this.payloadProvider.getPayloadSize ();
        debug ( "data - %s - %s%n", headerSize, payloadSize );

        // set signature data

        final Header<RpmSignatureTag> signature = new Header<RpmSignatureTag> ();

        // the order is important

        signature.putSize ( headerSize + payloadSize, RpmSignatureTag.SIZE, RpmSignatureTag.LONGSIZE );
        signature.putBlob ( RpmSignatureTag.MD5, makeMd5Checksum () );
        signature.putString ( RpmSignatureTag.SHA1HEADER, makeSha1HeaderChecksum () );
        signature.putSize ( this.payloadProvider.getArchiveSize (), RpmSignatureTag.PAYLOAD_SIZE, RpmSignatureTag.LONGARCHIVESIZE );

        // write lead

        writeLead ();

        // write signature header

        writeSignatureHeader ( signature );

        // write the header

        debug ( "package - offset: %s%n", this.file.position () );
        safeWrite ( this.header.slice () ); // write sliced to keep the original position

        debug ( "payload - offset: %s%n", this.file.position () );

        // now append payload data

        try ( ReadableByteChannel payloadChannel = this.payloadProvider.openChannel () )
        {
            if ( payloadChannel instanceof FileChannel )
            {
                final long count = ( (FileChannel)payloadChannel ).transferTo ( 0, Long.MAX_VALUE, this.file );
                debug ( "transfered - %s%n", count );
            }
            else
            {
                final long count = ByteStreams.copy ( payloadChannel, this.file );
                debug ( "copyied - %s%n", count );
            }
        }

        debug ( "end - offset: %s%n", this.file.position () );
    }

    private byte[] makeMd5Checksum () throws IOException
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance ( "MD5" );

            // feed header

            feedHeader ( md );

            // feed payload file

            try ( ReadableByteChannel payloadChannel = this.payloadProvider.openChannel () )
            {
                ByteStreams.copy ( new DigestInputStream ( Channels.newInputStream ( payloadChannel ), md ), ByteStreams.nullOutputStream () );
            }

            // digest

            return md.digest ();
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new RuntimeException ( e );
        }
    }

    private String makeSha1HeaderChecksum ()
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance ( "SHA1" );
            feedHeader ( md );
            return Rpms.toHex ( md.digest () ).toLowerCase ();
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void feedHeader ( final MessageDigest digest )
    {
        digest.update ( this.header.slice () );
    }

}
