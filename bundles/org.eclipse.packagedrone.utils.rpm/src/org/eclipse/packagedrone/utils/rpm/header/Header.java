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
package org.eclipse.packagedrone.utils.rpm.header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.eclipse.packagedrone.utils.rpm.ReadableHeader;
import org.eclipse.packagedrone.utils.rpm.RpmBaseTag;
import org.eclipse.packagedrone.utils.rpm.RpmTag;

public class Header<T extends RpmBaseTag> implements ReadableHeader<T>
{
    @FunctionalInterface
    public interface ArrayAllocator<T>
    {
        public T[] allocate ( int length );
    }

    @FunctionalInterface
    public interface Putter<T extends RpmBaseTag, V>
    {
        public void put ( Header<T> header, T tag, V[] values );
    }

    @FunctionalInterface
    public interface ToShortFunction<T>
    {
        public short applyAsShort ( T value );
    }

    private static final class I18nString
    {
        private final String value;

        public I18nString ( final String value )
        {
            this.value = value;
        }
    }

    private final Map<Integer, Object> entries = new LinkedHashMap<> ();

    public Header ( final HeaderEntry[] entries )
    {
        if ( entries != null )
        {
            for ( final HeaderEntry entry : entries )
            {
                this.entries.put ( entry.getTag (), entry );
            }
        }
    }

    public Header ( final Header<T> other )
    {
        Objects.requireNonNull ( other );

        this.entries.putAll ( other.entries );
    }

    public Header ()
    {
    }

    public int size ()
    {
        return this.entries.size ();
    }

    public void putNull ( final int tag )
    {
        this.entries.put ( tag, null );
    }

    public void putNull ( final T tag )
    {
        this.entries.put ( tag.getValue (), null );
    }

    public void putByte ( final int tag, final byte... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, value );
    }

    public void putByte ( final T tag, final byte... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), value );
    }

    public void putShort ( final int tag, final short... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, value );
    }

    public void putShort ( final T tag, final short... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), value );
    }

    public void putInt ( final int tag, final int... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, value );
    }

    public void putInt ( final T tag, final int... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), value );
    }

    public void putLong ( final int tag, final long... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, value );
    }

    public void putLong ( final T tag, final long... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), value );
    }

    public void putString ( final int tag, final String value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, value );
    }

    public void putString ( final T tag, final String value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), value );
    }

    public void putStringOptional ( final int tag, final String value )
    {
        if ( value == null )
        {
            return;
        }

        this.entries.put ( tag, value );
    }

    public void putStringOptional ( final T tag, final String value )
    {
        if ( value == null )
        {
            return;
        }

        this.entries.put ( tag.getValue (), value );
    }

    public void putStringArray ( final int tag, final String... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, value );
    }

    public void putStringArray ( final T tag, final String... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), value );
    }

    public void putI18nString ( final int tag, final String... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, Arrays.stream ( value ).map ( v -> new I18nString ( v ) ).toArray ( I18nString[]::new ) );
    }

    public void putI18nString ( final T tag, final String... value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), Arrays.stream ( value ).map ( v -> new I18nString ( v ) ).toArray ( I18nString[]::new ) );
    }

    public void putBlob ( final int tag, final byte[] value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, ByteBuffer.wrap ( value ) );
    }

    public void putBlob ( final int tag, final ByteBuffer value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag, value );
    }

    public void putBlob ( final T tag, final byte[] value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), ByteBuffer.wrap ( value ) );
    }

    public void putBlob ( final T tag, final ByteBuffer value )
    {
        Objects.requireNonNull ( value );

        this.entries.put ( tag.getValue (), value );
    }

    public void putSize ( long value, final T intTag, final T longTag )
    {
        Objects.requireNonNull ( intTag );
        Objects.requireNonNull ( longTag );

        if ( value <= 0 )
        {
            value = 0;
        }

        if ( value > Integer.MAX_VALUE )
        {
            putLong ( longTag, value );
        }
        else
        {
            putInt ( intTag, (int)value );
        }
    }

    public void remove ( final int tag )
    {
        this.entries.remove ( tag );
    }

    public void remove ( final RpmTag tag )
    {
        this.entries.remove ( tag.getValue () );
    }

    public Object get ( final int tag )
    {
        return this.entries.get ( tag );
    }

    public Object get ( final T tag )
    {
        return this.entries.get ( tag.getValue () );
    }

    @Override
    public Optional<Object> getValue ( final T tag )
    {
        return Optional.ofNullable ( get ( tag ) );
    }

    /**
     * Make an array of header entries
     * <p>
     * <strong>Note:</strong> Further updates on this instance will not update
     * the returned array. This is actually a copy of the current state.
     * </p>
     *
     * @return a new array of all header entries, unsorted
     */
    public HeaderEntry[] makeEntries ()
    {
        return this.entries.entrySet ().stream ().map ( Header::makeEntry ).toArray ( num -> new HeaderEntry[num] );
    }

    private static HeaderEntry makeEntry ( final Map.Entry<Integer, Object> entry )
    {
        final Object val = entry.getValue ();
        final int tag = entry.getKey ();

        if ( val instanceof HeaderEntry )
        {
            return (HeaderEntry)val;
        }

        // NULL

        if ( val == null )
        {
            return new HeaderEntry ( Type.NULL, tag, 0, null );
        }

        // FIXME: CHAR

        // BYTE

        if ( val instanceof byte[] )
        {
            final byte[] value = (byte[])val;
            return new HeaderEntry ( Type.BYTE, tag, value.length, value );
        }

        // SHORT

        if ( val instanceof short[] )
        {
            final short[] value = (short[])val;

            final byte[] data = new byte[value.length * 2];
            final ByteBuffer buffer = ByteBuffer.wrap ( data );
            for ( final short v : value )
            {
                buffer.putShort ( v );
            }

            return new HeaderEntry ( Type.SHORT, tag, value.length, data );
        }

        // INT

        if ( val instanceof int[] )
        {
            final int[] value = (int[])val;

            final byte[] data = new byte[value.length * 4];
            final ByteBuffer buffer = ByteBuffer.wrap ( data );
            for ( final int v : value )
            {
                buffer.putInt ( v );
            }

            return new HeaderEntry ( Type.INT, tag, value.length, data );
        }

        // LONG

        if ( val instanceof long[] )
        {
            final long[] value = (long[])val;

            final byte[] data = new byte[value.length * 8];
            final ByteBuffer buffer = ByteBuffer.wrap ( data );
            for ( final long v : value )
            {
                buffer.putLong ( v );
            }

            return new HeaderEntry ( Type.LONG, tag, value.length, data );
        }

        // STRING

        if ( val instanceof String )
        {
            final String value = (String)val;

            return new HeaderEntry ( Type.STRING, tag, 1, makeStringData ( new ByteArrayOutputStream (), value ).toByteArray () );
        }

        // BLOB

        if ( val instanceof ByteBuffer )
        {
            final ByteBuffer value = (ByteBuffer)val;
            byte[] data;
            if ( value.hasArray () )
            {
                data = value.array ();
            }
            else
            {
                data = new byte[value.remaining ()];
                value.get ( data );
            }
            return new HeaderEntry ( Type.BLOB, tag, data.length, data );
        }

        // STRING_ARRAY

        if ( val instanceof String[] )
        {
            final String[] value = (String[])val;

            return new HeaderEntry ( Type.STRING_ARRAY, tag, value.length, makeStringsData ( new ByteArrayOutputStream (), value ).toByteArray () );
        }

        // I18N_STRING

        if ( val instanceof I18nString[] )
        {
            final I18nString[] value = (I18nString[])val;

            return new HeaderEntry ( Type.I18N_STRING, tag, value.length, makeStringsData ( new ByteArrayOutputStream (), value ).toByteArray () );
        }

        throw new IllegalArgumentException ( String.format ( "Unable to process value type: %s", val.getClass () ) );
    }

    private static <T extends OutputStream> T makeStringData ( final T out, final String string )
    {
        try
        {
            if ( string != null )
            {
                out.write ( string.getBytes ( StandardCharsets.UTF_8 ) );
            }
            out.write ( 0 );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( e );
        }
        return out;
    }

    private static <T extends OutputStream> T makeStringsData ( final T out, final String[] strings )
    {
        for ( final String s : strings )
        {
            makeStringData ( out, s );
        }
        return out;
    }

    private static <T extends OutputStream> T makeStringsData ( final T out, final I18nString[] strings )
    {
        for ( final I18nString s : strings )
        {
            if ( s != null )
            {
                makeStringData ( out, s.value );
            }
            else
            {
                makeStringData ( out, null );
            }
        }
        return out;
    }

    public static <E, V, T extends RpmBaseTag> void putFields ( final Header<T> header, final Collection<E> entries, final T tag, final ArrayAllocator<V> arrayAllocator, final Function<E, V> func, final Putter<T, V> putter )
    {
        if ( entries.isEmpty () )
        {
            return;
        }

        final V[] values = arrayAllocator.allocate ( entries.size () );
        int i = 0;
        for ( final E entry : entries )
        {
            values[i] = func.apply ( entry );
            i++;
        }

        putter.put ( header, tag, values );
    }

    public static <E, T extends RpmBaseTag> void putShortFields ( final Header<T> header, final Collection<E> entries, final T tag, final ToShortFunction<E> func )
    {
        if ( entries.isEmpty () )
        {
            return;
        }

        final short[] values = new short[entries.size ()];
        int i = 0;
        for ( final E entry : entries )
        {
            values[i] = func.applyAsShort ( entry );
            i++;
        }

        header.putShort ( tag, values );
    }

    public static <E, T extends RpmBaseTag> void putIntFields ( final Header<T> header, final Collection<E> entries, final T tag, final ToIntFunction<E> func )
    {
        if ( entries.isEmpty () )
        {
            return;
        }

        final int[] values = new int[entries.size ()];
        int i = 0;
        for ( final E entry : entries )
        {
            values[i] = func.applyAsInt ( entry );
            i++;
        }

        header.putInt ( tag, values );
    }

    public static <E, T extends RpmBaseTag> void putLongFields ( final Header<T> header, final Collection<E> entries, final T tag, final ToLongFunction<E> func )
    {
        if ( entries.isEmpty () )
        {
            return;
        }

        final long[] values = new long[entries.size ()];
        int i = 0;
        for ( final E entry : entries )
        {
            values[i] = func.applyAsLong ( entry );
            i++;
        }

        header.putLong ( tag, values );
    }

}
