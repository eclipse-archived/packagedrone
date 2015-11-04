/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.spool;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.packagedrone.repo.utils.IOConsumer;
import org.eclipse.packagedrone.repo.utils.IOFunction;

import java.util.Set;

import com.google.common.io.BaseEncoding;

public class OutputSpooler
{

    public class RecordingDigestOutputStream extends DigestOutputStream
    {
        private final String key;

        public RecordingDigestOutputStream ( final OutputStream stream, final MessageDigest digest, final String key )
        {
            super ( stream, digest );
            this.key = key;
        }

        @Override
        public void close () throws IOException
        {
            super.close ();

            final MessageDigest digest = getMessageDigest ();
            final byte[] result = digest.digest ();

            setResult ( this.key, result );
        }
    }

    public class CountingOutputStream extends FilterOutputStream
    {
        private final String key;

        private long count;

        public CountingOutputStream ( final String key, final OutputStream out )
        {
            super ( out );
            this.key = key;
        }

        @Override
        public void write ( final byte[] b, final int off, final int len ) throws IOException
        {
            this.out.write ( b, off, len );
            this.count += len;
        }

        @Override
        public void write ( final int b ) throws IOException
        {
            this.out.write ( b );
            this.count++;
        }

        @Override
        public void close () throws IOException
        {
            super.close ();
            setResultSize ( this.key, this.count );
        }

    }

    private class MultiplexStream extends OutputStream
    {
        private final OutputStream[] streams;

        public MultiplexStream ( final List<OutputStream> streams )
        {
            this.streams = streams.toArray ( new OutputStream[streams.size ()] );
        }

        @Override
        public void write ( final int b ) throws IOException
        {
            write ( new byte[] { (byte) ( b & 0xFF ) } );
        }

        @Override
        public void write ( final byte[] b, final int off, final int len ) throws IOException
        {
            forEach ( stream -> stream.write ( b, off, len ) );
        }

        @Override
        public void flush () throws IOException
        {
            forEach ( OutputStream::flush );
        }

        @Override
        public void close () throws IOException
        {
            final java.util.stream.Stream<OutputStream> s = Arrays.stream ( this.streams );
            OutputSpooler.closeAll ( s );
        }

        protected void forEach ( final IOConsumer<OutputStream> consumer ) throws IOException
        {
            for ( final OutputStream stream : this.streams )
            {
                consumer.accept ( stream );
            }
        }
    }

    private static class OutputEntry
    {
        private final String mimeType;

        private final IOFunction<OutputStream, OutputStream> transformer;

        public OutputEntry ( final String mimeType, final IOFunction<OutputStream, OutputStream> transformer )
        {
            this.mimeType = mimeType;
            this.transformer = transformer;
        }

        public String getMimeType ()
        {
            return this.mimeType;
        }

        public IOFunction<OutputStream, OutputStream> getTransformer ()
        {
            return this.transformer;
        }
    }

    private final Set<String> digests = new HashSet<> ();

    private final SpoolOutTarget target;

    private final Map<String, OutputEntry> outputs = new HashMap<> ();

    private final Map<String, String> checksums = new HashMap<> ();

    private final Map<String, Long> sizes = new HashMap<> ();

    public OutputSpooler ( final SpoolOutTarget target )
    {
        this.target = target;
    }

    public void addDigest ( final String algorithm )
    {
        this.digests.add ( algorithm );
    }

    public void addOutput ( final String fileName, final String mimeType )
    {
        addOutput ( fileName, mimeType, null );
    }

    public void addOutput ( final String fileName, final String mimeType, final IOFunction<OutputStream, OutputStream> transformer )
    {
        if ( transformer == null )
        {
            this.outputs.put ( fileName, new OutputEntry ( mimeType, output -> output ) );
        }
        else
        {
            this.outputs.put ( fileName, new OutputEntry ( mimeType, transformer ) );
        }
    }

    public void open ( final IOConsumer<OutputStream> consumer ) throws IOException
    {
        final List<OutputStream> streams = new LinkedList<> ();

        final Iterator<Entry<String, OutputEntry>> entries = this.outputs.entrySet ().iterator ();

        openNext ( streams, entries, stream -> {
            try ( final MultiplexStream multiplexStream = new MultiplexStream ( streams ) )
            {
                consumer.accept ( multiplexStream );
            }
        } );
    }

    protected void openNext ( final List<OutputStream> streams, final Iterator<Entry<String, OutputEntry>> entries, final IOConsumer<List<OutputStream>> streamsConsumer ) throws IOException
    {
        if ( !entries.hasNext () )
        {
            streamsConsumer.accept ( streams );
        }
        else
        {
            final Entry<String, OutputEntry> entry = entries.next ();
            this.target.spoolOut ( entry.getKey (), entry.getValue ().getMimeType (), stream -> {

                // add digesters

                for ( final String algo : this.digests )
                {
                    final String key = entry.getKey () + ":" + algo;
                    try
                    {
                        stream = new RecordingDigestOutputStream ( stream, MessageDigest.getInstance ( algo ), key );
                    }
                    catch ( final NoSuchAlgorithmException e )
                    {
                        throw new IOException ( e );
                    }
                }

                // add counter

                stream = new CountingOutputStream ( entry.getKey (), stream );

                // apply transformer

                stream = entry.getValue ().getTransformer ().apply ( stream );

                // add stream

                streams.add ( stream );

                // next

                openNext ( streams, entries, streamsConsumer );
            } );

        }
    }

    private void setResult ( final String key, final byte[] result )
    {
        this.checksums.put ( key, BaseEncoding.base16 ().lowerCase ().encode ( result ) );
    }

    private void setResultSize ( final String key, final long length )
    {
        this.sizes.put ( key, length );
    }

    static void closeAll ( final java.util.stream.Stream<OutputStream> stream ) throws IOException
    {
        final List<Exception> ex = new LinkedList<> ();

        stream.forEach ( s -> {
            try
            {
                s.close ();
            }
            catch ( final IOException e )
            {
                ex.add ( e );
            }
        } );

        if ( !ex.isEmpty () )
        {
            final IOException base = new IOException ();
            for ( final Exception e : ex )
            {
                base.addSuppressed ( e );
            }
            throw base;
        }
    }

    /**
     * Get the digest of a closed file
     *
     * @param fileName
     *            the file name to get the digest for
     * @param algorithm
     *            the digest algorithm
     * @return the digest or <code>null</code> if the digest was not requested.
     *         The digest will be lower case hex encoded.
     * @throws IllegalStateException
     *             If the file is still open or was never opened
     */
    public String getChecksum ( final String fileName, final String algorithm )
    {
        if ( !this.digests.contains ( algorithm ) )
        {
            return null;
        }

        final String result = this.checksums.get ( fileName + ":" + algorithm );

        if ( result == null )
        {
            throw new IllegalStateException ( String.format ( "Stream '%s' not closed.", fileName ) );
        }

        return result;
    }

    /**
     * Get the size of a closed file
     *
     * @param fileName
     *            the file name to get the size for
     * @return the size
     * @throws IllegalStateException
     *             If the file is still open or was never opened
     */
    public long getSize ( final String fileName )
    {
        final Long result = this.sizes.get ( fileName );

        if ( result == null )
        {
            throw new IllegalStateException ( String.format ( "Stream '%s' not closed or was not added", fileName ) );
        }

        return result;
    }
}
