/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

public class Rpms
{
    private final static char[] HEX = "0123456789ABCDEF".toCharArray ();

    public static final byte[] LEAD_MAGIC = new byte[] { (byte)0xED, (byte)0xAB, (byte)0xEE, (byte)0xDB };

    public static final byte[] HEADER_MAGIC = new byte[] { (byte)0x8E, (byte)0xAD, (byte)0xE8 };

    public static final byte[] EMPTY_128;

    public static final int IMMUTABLE_TAG_SIGNATURE = 62;

    public static final int IMMUTABLE_TAG_HEADER = 63;

    static
    {
        EMPTY_128 = new byte[128];
        Arrays.fill ( EMPTY_128, (byte)0 );
    }

    public static String toHex ( final byte[] data )
    {
        return toHex ( data, Integer.MAX_VALUE );
    }

    public static String toHex ( final byte[] data, final int maxWidth )
    {
        return toHex ( data, 0, data.length, maxWidth );
    }

    public static String toHex ( final byte[] data, final int offset, final int length, final int maxWidth )
    {
        final StringBuilder sb = new StringBuilder ( length * 2 ); // not considering line breaks

        int lc = 0;
        for ( int i = 0; i < length; i++ )
        {
            if ( maxWidth > 0 && lc >= maxWidth )
            {
                sb.append ( System.lineSeparator () );
                lc = 0;
            }

            final int b = data[offset + i] & 0xFF;
            sb.append ( HEX[b >>> 4] );
            sb.append ( HEX[b & 0x0F] );
            lc++;
        }

        return sb.toString ();
    }

    public static String dumpValue ( final Object value )
    {
        final StringBuilder sb = new StringBuilder ();
        dumpValue ( sb, value );
        return sb.toString ();
    }

    static void writeByteBuffer ( final OutputStream stream, final ByteBuffer dataStore ) throws IOException
    {
        final WritableByteChannel c = Channels.newChannel ( stream );
        while ( dataStore.hasRemaining () )
        {
            c.write ( dataStore );
        }
    }

    public static void dumpValue ( final StringBuilder sb, final Object value )
    {
        if ( value != null )
        {
            if ( value instanceof byte[] )
            {
                sb.append ( toHex ( (byte[])value, -1 ) );
            }
            else if ( value.getClass ().isArray () )
            {
                sb.append ( Arrays.toString ( (Object[])value ) );
            }
            else
            {
                sb.append ( value );
            }
        }
        else
        {
            sb.append ( "null" );
        }
    }

}
