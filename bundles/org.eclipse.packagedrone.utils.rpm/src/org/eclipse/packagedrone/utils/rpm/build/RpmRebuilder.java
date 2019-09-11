/*******************************************************************************
 * Copyright (c) 2019 Trident Systems, Inc.
 * This software was developed with U.S government funding in support of the above
 * contract.  Trident grants unlimited rights to modify, distribute and incorporate
 * our contributions to Eclipse Package Drone bound by the overall restrictions from
 * the parent Eclipse Public License v1.0 available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Walker Funk - Trident Systems Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.eclipse.packagedrone.utils.rpm.header.Header;
import org.eclipse.packagedrone.utils.rpm.header.Headers;
import org.eclipse.packagedrone.utils.rpm.signature.SignatureProcessor;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A low level RPM file rebuilder
 * <p>
 * This class handles the reconstructing of RPM files. It does not really care about the
 * contents it writes but the content and the format of the content should come
 * from a pre-parsed rpm (ideally via the RpmParserStreamer class) and
 * added signature processor(s)
 * </p>
 */
public class RpmRebuilder implements AutoCloseable
{
    private static final OpenOption[] DEFAULT_OPEN_OPTIONS = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING };

    private final static Logger logger = LoggerFactory.getLogger ( RpmRebuilder.class );

    private final FileChannel file;

    private final RpmLead lead;

    private final Header<RpmSignatureTag> signatureHeader;

    private final ByteBuffer header;

    private boolean finished;

    private PayloadStreamer payloadStreamer;

    private final List<SignatureProcessor> signatureProcessors = new LinkedList<> ();

    public RpmRebuilder ( final Path path, final RpmLead lead, final Header<RpmSignatureTag> signatureHeader, final Header<RpmTag> header ) throws IOException
    {
        requireNonNull ( path );
        requireNonNull ( lead );
        requireNonNull ( header );
        requireNonNull ( signatureHeader );

        this.file = FileChannel.open ( path, DEFAULT_OPEN_OPTIONS );
        this.lead = lead;
        this.signatureHeader = signatureHeader;

        this.header = Headers.render ( header.makeEntries (), true, Rpms.IMMUTABLE_TAG_HEADER );
    }

    public void addSignatureProcessor ( final SignatureProcessor processor )
    {
        this.signatureProcessors.add ( processor );
    }

    public void addAllSignatureProcessors ( final List<SignatureProcessor> signatureProcessors )
    {
        this.signatureProcessors.addAll ( signatureProcessors );
    }

    public void setPayload ( final PayloadStreamer payloadStreamer ) throws IOException
    {
        checkNotFinished ();

        requireNonNull ( payloadStreamer );

        this.payloadStreamer = payloadStreamer;
    }

    private void checkNotFinished ()
    {
        if ( this.finished )
        {
            throw new IllegalStateException ( "Writing of RPM is already finished" );
        }
    }

    private static void debug ( final String fmt, final Object... args )
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

        // 2 bytes type

        lead.putShort ( this.lead.getType () );

        // 2 bytes arch

        lead.putShort ( this.lead.getArchitecture () );

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

        // 2 bytes OS

        lead.putShort ( this.lead.getOperatingSystem () );

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

        final ByteBuffer buffer = Headers.render ( header.makeEntries (), true, Rpms.IMMUTABLE_TAG_SIGNATURE );

        final int payloadSize = buffer.remaining ();

        // header

        debug ( "start header - offset: %s, len: %s", this.file.position (), payloadSize );
        safeWrite ( buffer );

        // padding

        final int padding = Rpms.padding ( payloadSize );

        if ( padding > 0 )
        {
            safeWrite ( ByteBuffer.wrap ( Rpms.EMPTY_128, 0, padding ) );
            debug ( "write - padding - %s", padding );
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
            this.payloadStreamer.close ();
        }
    }

    private void finish () throws IOException
    {
        if ( this.finished )
        {
            return;
        }

        if ( this.payloadStreamer == null )
        {
            throw new IOException ( "Unable to finish RPM file, payload provider not set" );
        }

        this.finished = true;

        final int headerSize = this.header.remaining ();
        final long payloadSize = this.payloadStreamer.getPayloadSize ();
        debug ( "data - %s - %s", headerSize, payloadSize );

        // process signatures

        processSignatures ( this.signatureHeader );

        // write lead

        writeLead ();

        // write signature header

        writeSignatureHeader ( this.signatureHeader );

        // write the header

        debug ( "package - offset: %s", this.file.position () );
        safeWrite ( this.header.slice () ); // write sliced to keep the original position

        debug ( "payload - offset: %s", this.file.position () );

        // now append payload data

        try ( ReadableByteChannel payloadChannel = this.payloadStreamer.openChannel () )
        {
            if ( payloadChannel instanceof FileChannel && !isForceCopy () )
            {
                final long count = copyFileChannel ( (FileChannel)payloadChannel, this.file );
                debug ( "transfered - %s", count );
            }
            else
            {
                final long count = ByteStreams.copy ( payloadChannel, this.file );
                debug ( "copyied - %s", count );
            }
        }

        debug ( "end - offset: %s", this.file.position () );
    }

    /**
     * Check of in-JVM copy should be forced over
     * {@link FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)}
     *
     * @return {@code true} if copying should be forced, {@code false}
     *         otherwise. Defaults to {@code false}.
     */
    private static boolean isForceCopy ()
    {
        return Boolean.getBoolean ( "org.eclipse.packagedrone.utils.rpm.build.RpmRebuilder.forceCopy" );
    }

    private static long copyFileChannel ( final FileChannel fileChannel, final FileChannel file ) throws IOException
    {
        long remaning = fileChannel.size ();
        long position = 0;

        while ( remaning > 0 )
        {
            // transfer next chunk

            final long rc = fileChannel.transferTo ( position, remaning, file );

            // check for negative result

            if ( rc < 0 )
            {
                throw new IOException ( String.format ( "Failed to transfer bytes: rc = %s", rc ) );
            }

            debug ( "transferTo - position: %s, size: %s => rc: %s", position, remaning, rc );

            // we should never get zero back, but check anyway

            if ( rc == 0 )
            {
                break;
            }

            // update state

            position += rc;
            remaning -= rc;
        }

        // final check if we got it all

        if ( remaning > 0 )
        {
            throw new IOException ( "Failed to transfer full content" );
        }

        return position;
    }

    private void processSignatures ( final Header<RpmSignatureTag> signature ) throws IOException
    {
        // init

        // for ( final SignatureProcessor processor : this.signatureProcessors )
        // {
        //    processor.init ( this.payloadStreamer.getArchiveSize () );
        // }

        // feed the header

        for ( final SignatureProcessor processor : this.signatureProcessors )
        {
            processor.feedHeader ( this.header.slice () );
        }

        // feed payload data

        try ( ReadableByteChannel channel = this.payloadStreamer.openChannel () )
        {
            final ByteBuffer buf = ByteBuffer.wrap ( new byte[4096] );

            while ( channel.read ( buf ) >= 0 )
            {
                buf.flip ();
                for ( final SignatureProcessor processor : this.signatureProcessors )
                {
                    processor.feedPayloadData ( buf.slice () );
                }
                buf.clear ();
            }
        }

        // finish up

        for ( final SignatureProcessor processor : this.signatureProcessors )
        {
            processor.finish ( signature );
        }
    }

}
