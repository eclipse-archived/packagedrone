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
package org.eclipse.packagedrone.utils;

import java.math.RoundingMode;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;

public final class Strings
{

    private static final Format bytesPattern = new MessageFormat ( "{0,choice,0#0 bytes|1#1 byte|1<{0,number,integer} bytes}" );

    private static final NumberFormat numberPattern1 = NumberFormat.getNumberInstance ();

    private static final NumberFormat numberPattern2 = NumberFormat.getNumberInstance ();

    static
    {
        numberPattern1.setRoundingMode ( RoundingMode.HALF_UP );
        numberPattern1.setMaximumFractionDigits ( 1 );
        numberPattern1.setGroupingUsed ( false );

        numberPattern2.setRoundingMode ( RoundingMode.HALF_UP );
        numberPattern2.setMaximumFractionDigits ( 2 );
        numberPattern2.setGroupingUsed ( false );
    }

    private Strings ()
    {
    }

    public static String hex ( final byte[] digest )
    {
        final StringBuilder sb = new StringBuilder ( digest.length * 2 );

        for ( int i = 0; i < digest.length; i++ )
        {
            sb.append ( String.format ( "%02x", digest[i] & 0xFF ) );
        }

        return sb.toString ();
    }

    public static String bytes ( final long amount )
    {
        if ( amount < 1024L )
        {
            return bytesPattern.format ( new Object[] { amount } );
        }
        if ( amount < 1024L * 1024L )
        {
            return numberPattern1.format ( amount / 1024.0 ) + " KiB";
        }
        if ( amount < 1024L * 1024L * 1024L )
        {
            return numberPattern2.format ( amount / ( 1024.0 * 1024.0 ) ) + " MiB";
        }
        return numberPattern2.format ( amount / ( 1024.0 * 1024.0 * 1024.0 ) ) + " GiB";
    }
}
