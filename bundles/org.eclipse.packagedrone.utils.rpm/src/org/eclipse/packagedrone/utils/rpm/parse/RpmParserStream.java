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
package org.eclipse.packagedrone.utils.rpm.parse;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.packagedrone.utils.rpm.RpmBaseTag;
import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.eclipse.packagedrone.utils.rpm.header.*;
import org.eclipse.packagedrone.utils.rpm.build.PayloadStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.CountingInputStream;

public class RpmParserStream extends InputStream
{
    private final static Logger logger = LoggerFactory.getLogger ( RpmParserStream.class );

    private static final byte[] DUMMY = new byte[128];

    private final DataInputStream in;

    private boolean closed;

    private RpmLead lead;

    private Header<RpmSignatureTag> signatureHeader;

    private Header<RpmTag> payloadHeader;

    private PayloadStreamer payloadStreamer;

    private final CountingInputStream count;

    public RpmParserStream ( final InputStream in )
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
        if ( this.payloadStreamer == null )
        {
            this.payloadStreamer = new PayloadStreamer( true, this.in );
        }
    }

    public PayloadStreamer getPayloadStreamer ()
    {
        return this.payloadStreamer;
    }

    public RpmLead getLead () throws IOException
    {
        ensureInit ();
        return this.lead;
    }

    public Header<RpmSignatureTag> getSignatureHeader () throws IOException
    {
        ensureInit ();
        return this.signatureHeader;
    }

    public Header<RpmTag> getPayloadHeader () throws IOException
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

    protected <T extends RpmBaseTag> Header<T> readHeader ( final boolean withPadding ) throws IOException
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

        final int indexCount = (this.in.readInt () - 1); // Subtract one to account for immutable
        final int storeSize = this.in.readInt ();

        skipFully ( 16 ); // IMMUTABLE

        final ByteBuffer tags = ByteBuffer.wrap ( readComplete ( (indexCount * 16) ) );
        final ByteBuffer store = ByteBuffer.wrap ( readComplete ( storeSize ) );

        final HeaderEntry[] entries = new HeaderEntry[indexCount];
        for ( int i = 0; i < indexCount; i++ )
        {
            entries[i] = readEntry ( tags, store );
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

        return new Header<> ( entries );
    }

    private HeaderEntry readEntry ( ByteBuffer tags, ByteBuffer store ) throws IOException
    {
        final int tag = tags.getInt ();
        final int dataType = tags.getInt ();
        final int offset = tags.getInt ();
        final int count = tags.getInt ();


        final Type type;
        byte[] data = null;
        switch ( dataType )
        {
            case 1:
                type = Type.CHAR;
                data = new byte[count];
                store.position ( offset );
                store.get ( data );
                break;
            case 2:
                type = Type.BYTE;
                data = new byte[count];
                store.position ( offset );
                store.get ( data );
                break;
            case 3:
                type = Type.SHORT;
                data = new byte[2 * count];
                store.position ( offset );
                store.get ( data );
                break;
            case 4:
                type = Type.INT;
                data = new byte[4 * count];
                store.position ( offset );
                store.get ( data );
                break;
            case 5:
                type = Type.LONG;
                data = new byte[8 * count];
                store.position ( offset );
                store.get ( data );
                break;
            case 6:
                type = Type.STRING;
                data = getBinStringData ( store, offset, 1 );
                break;
            case 7:
                type = Type.BLOB;
                data = new byte[count];
                store.position ( offset );
                store.get ( data );
                break;
            case 8:
                type = Type.STRING_ARRAY;
                data = getBinStringData ( store, offset, count );
                break;
            case 9:
                type = Type.I18N_STRING;
                data = getBinStringData ( store, offset, count );
                break;
            default:
                type = Type.NULL; // Null or Unknown (both unsupported)
                break;
        }
        if (data == null)
        {
            throw new IOException ( "Null or unknown data type" );
        }
        return new HeaderEntry ( type, tag, count, data );
    }

    private byte[] getBinStringData ( final ByteBuffer store, int offset, final int strings ) throws IOException
    {
        int num = 0;
        store.position ( offset );
        for ( int i = 0; i < store.remaining (); i++ ) // Find null terminated string or sequence of null terminated strings
        {
            if ( store.get ( offset + i ) == 0 )
            {
               num++;
            }
            if ( num == strings )
            {
                byte[] data = new byte[i + 1];
                store.get ( data );
                return data;
            }
        }
        throw new IOException ( "Corrupt tag entry" );
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
        this.in.reset ();
    }

    @Override
    public int read () throws IOException
    {
        ensureInit ();
        return this.in.read ();
    }

    @Override
    public long skip ( final long n ) throws IOException
    {
        ensureInit ();
        return this.in.skip ( n );
    }

    @Override
    public int available () throws IOException
    {
        ensureInit ();
        return this.in.available ();
    }

    @Override
    public int read ( final byte[] b ) throws IOException
    {
        ensureInit ();
        return this.in.read ( b );
    }

    @Override
    public int read ( final byte[] b, final int off, final int len ) throws IOException
    {
        return this.in.read ( b, off, len );
    }

}
