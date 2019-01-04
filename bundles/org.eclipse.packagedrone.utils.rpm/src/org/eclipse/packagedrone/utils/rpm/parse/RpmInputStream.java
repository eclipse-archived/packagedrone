/*******************************************************************************
 * Copyright (c) 2015, 2018 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.parse;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.eclipse.packagedrone.utils.rpm.RpmBaseTag;
import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.eclipse.packagedrone.utils.rpm.coding.PayloadCoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CountingInputStream;

public class RpmInputStream extends InputStream
{
    private final static Logger logger = LoggerFactory.getLogger ( RpmInputStream.class );

    private static final byte[] DUMMY = new byte[128];

    private final DataInputStream in;

    private boolean closed;

    private RpmLead lead;

    private InputHeader<RpmSignatureTag> signatureHeader;

    private InputHeader<RpmTag> payloadHeader;

    private InputStream payloadStream;

    private CpioArchiveInputStream cpioStream;

    private final CountingInputStream count;

    public RpmInputStream ( final InputStream in )
    {
        this.count = new CountingInputStream ( in );
        this.in = new DataInputStream ( this.count );
    }

    @Override
    public void close () throws IOException
    {
        if ( !this.closed )
        {
            this.in.close ();
            this.closed = true;
        }
    }

    protected void ensureInit () throws IOException
    {
        if ( this.lead == null )
        {
            this.lead = readLead ();
        }

        if ( this.signatureHeader == null )
        {
            this.signatureHeader = readHeader ( true );
        }

        if ( this.payloadHeader == null )
        {
            this.payloadHeader = readHeader ( false );
        }

        // set up content stream

        if ( this.payloadStream == null )
        {
            this.payloadStream = setupPayloadStream ();
            this.cpioStream = new CpioArchiveInputStream ( this.payloadStream, "UTF-8" ); // we did ensure that we only support CPIO before
        }
    }

    private InputStream setupPayloadStream () throws IOException
    {

        // payload format

        final Object payloadFormatValue = this.payloadHeader.getTag ( RpmTag.PAYLOAD_FORMAT );

        if ( payloadFormatValue != null && ! ( payloadFormatValue instanceof String ) )
        {
            throw new IOException ( "Payload format must be a single string" );
        }

        String payloadFormat = (String)payloadFormatValue;

        if ( payloadFormat == null )
        {
            payloadFormat = "cpio";
        }

        if ( !"cpio".equals ( payloadFormat ) )
        {
            throw new IOException ( String.format ( "Unknown payload format: %s", payloadFormat ) );
        }

        // payload coding

        final Optional<Object> payloadCodingHeader = this.payloadHeader.getOptionalTag ( RpmTag.PAYLOAD_CODING );

        if ( !payloadCodingHeader.isPresent () )
        {
            return this.in;
        }

        final Object payloadCodingValue = payloadCodingHeader.get ();

        final String payloadCoding = (String)payloadCodingValue;

        if ( payloadCodingValue != null && ! ( payloadCodingValue instanceof String ) )
        {
            throw new IOException ( "Payload coding must be a single string" );
        }

        final PayloadCoding coding = PayloadCoding.fromValue ( payloadCoding ).orElseThrow ( () -> new IOException ( String.format ( "Unknown payload coding: '%s'", payloadCoding ) ) );

        return coding.createProvider ().createInputStream ( this.in );
    }

    public CpioArchiveInputStream getCpioStream ()
    {
        return this.cpioStream;
    }

    public RpmLead getLead () throws IOException
    {
        ensureInit ();
        return this.lead;
    }

    public InputHeader<RpmSignatureTag> getSignatureHeader () throws IOException
    {
        ensureInit ();
        return this.signatureHeader;
    }

    public InputHeader<RpmTag> getPayloadHeader () throws IOException
    {
        ensureInit ();
        return this.payloadHeader;
    }

    protected RpmLead readLead () throws IOException
    {
        final byte[] magic = readComplete ( 4 );

        if ( !Arrays.equals ( magic, Rpms.LEAD_MAGIC ) )
        {
            throw new IOException ( String.format ( "File corrupt: Expected magic %s, read: %s", Arrays.toString ( Rpms.LEAD_MAGIC ), Arrays.toString ( magic ) ) );
        }

        final byte[] version = readComplete ( 2 );

        final short type = this.in.readShort ();
        final short arch = this.in.readShort ();

        final byte[] nameData = readComplete ( 66 ); // NAME

        final ByteBuffer nameBuffer = ByteBuffer.wrap ( nameData );
        for ( int i = 0; i < nameData.length; i++ )
        {
            if ( nameData[i] == 0 )
            {
                nameBuffer.limit ( i );
                break;
            }
        }

        final String name = StandardCharsets.UTF_8.decode ( nameBuffer ).toString ();

        final short os = this.in.readShort ();

        final int sigType = this.in.readUnsignedShort ();

        skipFully ( 16 ); // RESERVED

        return new RpmLead ( version[0], version[1], name, sigType, type, arch, os );
    }

    protected <T extends RpmBaseTag> InputHeader<T> readHeader ( final boolean withPadding ) throws IOException
    {
        final long start = this.count.getCount ();

        final byte[] magic = readComplete ( 3 );

        if ( !Arrays.equals ( magic, Rpms.HEADER_MAGIC ) )
        {
            throw new IOException ( String.format ( "File corrupt: Expected entry magic %s, read: %s", Arrays.toString ( Rpms.HEADER_MAGIC ), Arrays.toString ( magic ) ) );
        }

        final byte version = this.in.readByte ();
        if ( version != 1 )
        {
            throw new IOException ( String.format ( "File corrupt: Invalid header entry version: %s (valid: 1)", version ) );
        }

        skipFully ( 4 ); // RESERVED

        final int indexCount = this.in.readInt ();
        final int storeSize = this.in.readInt ();

        final HeaderValue[] entries = new HeaderValue[indexCount];

        for ( int i = 0; i < indexCount; i++ )
        {
            entries[i] = readEntry ();
        }

        final ByteBuffer store = ByteBuffer.wrap ( readComplete ( storeSize ) );

        for ( int i = 0; i < indexCount; i++ )
        {
            entries[i].fillFromStore ( store );
        }

        if ( withPadding )
        {
            // pad remaining bytes - to 8

            final int skip = Rpms.padding ( storeSize );
            if ( skip > 0 )
            {
                logger.debug ( "Skipping {} pad bytes", skip );
                skipFully ( skip );
            }
        }

        final long end = this.count.getCount ();

        return new InputHeader<> ( entries, start, end - start );
    }

    private HeaderValue readEntry () throws IOException
    {
        final int tag = this.in.readInt ();
        final int type = this.in.readInt ();
        final int offset = this.in.readInt ();
        final int count = this.in.readInt ();

        return new HeaderValue ( tag, type, offset, count );
    }

    private byte[] readComplete ( final int size ) throws IOException
    {
        final byte[] result = new byte[size];
        this.in.readFully ( result );
        return result;
    }

    private void skipFully ( final int count ) throws IOException
    {
        if ( count > 0 )
        {
            this.in.readFully ( DUMMY, 0, count );
        }
    }

    // forward methods

    @Override
    public void reset () throws IOException
    {
        ensureInit ();
        this.payloadStream.reset ();
    }

    @Override
    public int read () throws IOException
    {
        ensureInit ();
        return this.payloadStream.read ();
    }

    @Override
    public long skip ( final long n ) throws IOException
    {
        ensureInit ();
        return this.payloadStream.skip ( n );
    }

    @Override
    public int available () throws IOException
    {
        ensureInit ();
        return this.payloadStream.available ();
    }

    @Override
    public int read ( final byte[] b ) throws IOException
    {
        ensureInit ();
        return this.payloadStream.read ( b );
    }

    @Override
    public int read ( final byte[] b, final int off, final int len ) throws IOException
    {
        return this.payloadStream.read ( b, off, len );
    }

}
