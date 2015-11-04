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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class RpmEntry
{
    private static final class Unknown
    {
        @Override
        public String toString ()
        {
            return "UNKNOWN";
        }
    }

    public static final Unknown UNKNOWN = new Unknown ();

    private final int tag;

    private Object value;

    private final int type;

    private final int index;

    private final int count;

    public RpmEntry ( final int tag, final int type, final int index, final int count )
    {
        this.tag = tag;
        this.type = type;
        this.index = index;
        this.count = count;
    }

    public int getTag ()
    {
        return this.tag;
    }

    public Object getValue ()
    {
        return this.value;
    }

    void fillFromStore ( final ByteBuffer storeData ) throws IOException
    {
        switch ( this.type )
        {
            case 0: // null value
                break;
            case 1: // character
                this.value = getFromStore ( storeData, buf -> (char)storeData.get (), size -> new Character[size] );
                break;
            case 2: // byte
                this.value = getFromStore ( storeData, buf -> buf.get (), size -> new Byte[size] );
                break;
            case 3: // unsigned 16bit integer
                this.value = getFromStore ( storeData, buf -> buf.getShort () & 0xFFFF, size -> new Integer[size] );
                break;
            case 4: // unsigned 32bit integer
                this.value = getFromStore ( storeData, buf -> (long) ( buf.getInt () & 0xFFFFFFFFL ), size -> new Long[size] );
                break;
            case 5: // unsigned 64bit integer
                this.value = getFromStore ( storeData, buf -> buf.getLong (), size -> new Long[size] );
                break;
            case 6: // one string
            {
                // only one allowed
                storeData.position ( this.index );
                this.value = makeString ( storeData );
            }
                break;
            case 7: // blob
            {
                final byte[] data = new byte[this.count];
                storeData.get ( data );
                this.value = data;
            }
                break;
            case 8: // string array
                this.value = getFromStore ( storeData, buf -> makeString ( buf ), size -> new String[size] );
                break;
            case 9: // i18n string array
                this.value = getFromStore ( storeData, buf -> makeString ( buf ), size -> new String[size] );
                break;
            default:
                this.value = UNKNOWN;
                break;
        }
    }

    @FunctionalInterface
    public static interface IOFunction<T, R>
    {
        public R apply ( T t ) throws IOException;
    }

    private <R> Object getFromStore ( final ByteBuffer data, final IOFunction<ByteBuffer, R> func, final Function<Integer, R[]> creator ) throws IOException
    {
        data.position ( this.index );
        if ( this.count == 1 )
        {
            return func.apply ( data );
        }

        final R[] result = creator.apply ( this.count );
        for ( int i = 0; i < this.count; i++ )
        {
            result[i] = func.apply ( data );
        }
        return result;
    }

    private static String makeString ( final ByteBuffer buf ) throws IOException
    {
        final byte[] data = buf.array ();
        final int start = buf.position ();

        for ( int i = 0; i < buf.remaining (); i++ ) // check if there is at least one more byte, null byte
        {
            if ( data[start + i] == 0 )
            {
                buf.position ( start + i + 1 ); // skip content plus null byte
                return new String ( data, start, i, StandardCharsets.UTF_8 );
            }
        }
        throw new IOException ( "Corrupt tag entry. Null byte missing!" );
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( '[' );
        sb.append ( this.tag );
        sb.append ( " = " );

        Rpms.dumpValue ( sb, this.value );

        if ( this.value != null )
        {
            if ( this.value != UNKNOWN )
            {
                sb.append ( " - " ).append ( this.value.getClass ().getName () );
            }
            else
            {
                sb.append ( " - " ).append ( this.type );
            }
        }
        else
        {
            sb.append ( "NULL" );
        }

        sb.append ( " | " );
        sb.append ( this.count );
        sb.append ( ']' );

        return sb.toString ();
    }

}
