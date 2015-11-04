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
package org.eclipse.packagedrone.utils.rpm;

import java.util.Arrays;

public class Rpms
{
    private final static char[] HEX = "0123456789ABCDEF".toCharArray ();

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
