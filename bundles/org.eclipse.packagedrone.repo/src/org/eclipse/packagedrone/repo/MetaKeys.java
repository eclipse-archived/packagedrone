/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.utils.converter.ConverterManager;

/**
 * Helper methods when working with {@link MetaKey}s
 */
public final class MetaKeys
{
    private MetaKeys ()
    {
    }

    /**
     * Get a string from meta data
     * <p>
     * If the provided metadata set is <code>null</code>, then the result will
     * also be <code>null</code>.
     * </p>
     *
     * @param metadata
     *            the meta data map, may be <code>null</code>
     * @param ns
     *            the namespace
     * @param key
     *            the key
     * @return the value, as string, may be <code>null</code>
     */
    public static String getString ( final Map<MetaKey, String> metadata, final String ns, final String key )
    {
        return getString ( metadata, ns, key, null );
    }

    /**
     * Get a string from meta data
     * <p>
     * If the provided metadata set is <code>null</code>, then the result will
     * also be <code>null</code>.
     * </p>
     *
     * @param metadata
     *            the meta data map, may be <code>null</code>
     * @param ns
     *            the namespace
     * @param key
     *            the key
     * @param defaultValue
     *            the value returned if either the metadata parmeter is
     *            <code>null</code> or the metadata does not contain this key or
     *            the value is <code>null</code>
     * @return the value, as string, may be <code>null</code>
     */
    public static String getString ( final Map<MetaKey, String> metadata, final String ns, final String key, final String defaultValue )
    {
        if ( metadata == null )
        {
            return defaultValue;
        }
        final String result = metadata.get ( new MetaKey ( ns, key ) );
        if ( result == null )
        {
            return defaultValue;
        }
        return result;
    }

    public static <T> T bind ( final T data, final Map<MetaKey, String> metadata ) throws Exception
    {
        final ConverterManager converter = ConverterManager.create ();

        if ( data == null )
        {
            return null;
        }

        final List<Field> fields = new LinkedList<> ();
        findFields ( data.getClass (), fields );

        for ( final Field field : fields )
        {
            final MetaKeyBinding mkb = field.getAnnotation ( MetaKeyBinding.class );
            final String stringValue = metadata.get ( new MetaKey ( mkb.namespace (), mkb.key () ) );

            final Object value;
            if ( stringValue == null )
            {
                value = null;
            }
            else
            {
                if ( !mkb.converterClass ().isInterface () )
                {
                    value = mkb.converterClass ().newInstance ().decode ( stringValue );
                }
                else
                {
                    value = converter.convertTo ( stringValue, field.getType () );
                }
            }

            if ( value != null || !mkb.ignoreNull () )
            {
                setValue ( field, data, value );
            }
        }

        return data;
    }

    public static final Map<MetaKey, String> unbind ( final Object data ) throws Exception
    {
        final ConverterManager converter = ConverterManager.create ();

        if ( data == null )
        {
            return null;
        }

        final List<Field> fields = new LinkedList<> ();
        findFields ( data.getClass (), fields );

        final Map<MetaKey, String> result = new HashMap<> ( fields.size () );

        for ( final Field field : fields )
        {
            final MetaKeyBinding mkb = field.getAnnotation ( MetaKeyBinding.class );
            final Object value = getValue ( field, data );

            final String stringValue;

            if ( !mkb.converterClass ().isInterface () )
            {
                final BindingConverter cvt = mkb.converterClass ().newInstance ();
                stringValue = cvt.encode ( value );
            }
            else
            {
                stringValue = converter.convertTo ( value, String.class );
            }

            if ( ( stringValue == null || stringValue.isEmpty () ) && mkb.emptyAsNull () )
            {
                result.put ( new MetaKey ( mkb.namespace (), mkb.key () ), null );
            }
            else
            {
                result.put ( new MetaKey ( mkb.namespace (), mkb.key () ), stringValue );
            }
        }

        return result;
    }

    private static Object getValue ( final Field field, final Object data ) throws IllegalArgumentException, IllegalAccessException
    {
        if ( field.isAccessible () )
        {
            return field.get ( data );
        }
        else
        {
            field.setAccessible ( true );
            try
            {
                return field.get ( data );
            }
            finally
            {
                field.setAccessible ( false );
            }
        }
    }

    private static void setValue ( final Field field, final Object target, final Object value ) throws IllegalArgumentException, IllegalAccessException
    {
        if ( field.isAccessible () )
        {
            internalSetField ( field, target, value );
        }
        else
        {
            field.setAccessible ( true );
            try
            {
                internalSetField ( field, target, value );
            }
            finally
            {
                field.setAccessible ( false );
            }
        }
    }

    protected static void internalSetField ( final Field field, final Object target, final Object value ) throws IllegalAccessException
    {
        if ( value != null || !field.getType ().isPrimitive () )
        {
            field.set ( target, value );
        }
    }

    private static void findFields ( final Class<?> clazz, final List<Field> result ) throws SecurityException
    {
        if ( clazz == null )
        {
            return;
        }

        for ( final Field f : clazz.getDeclaredFields () )
        {
            if ( f.isAnnotationPresent ( MetaKeyBinding.class ) )
            {
                result.add ( f );
            }
        }

        findFields ( clazz.getSuperclass (), result );
    }

    /**
     * Return an unmodifiable map of provided and extracted meta data
     *
     * @param providedMetaData
     *            the provided meta data, or code {@code null}
     * @param extractedMetaData
     *            the extracted meta data, or code {@code null}
     * @return the union of provided and extracted meta data, never returns
     *         {@code null}
     */
    public static Map<MetaKey, String> union ( final Map<MetaKey, String> providedMetaData, final Map<MetaKey, String> extractedMetaData )
    {
        final int size1 = providedMetaData != null ? providedMetaData.size () : 0;
        final int size2 = extractedMetaData != null ? extractedMetaData.size () : 0;

        if ( size1 + size2 == 0 )
        {
            return Collections.emptyMap ();
        }

        final Map<MetaKey, String> result = new HashMap<> ( size1 + size2 );

        if ( extractedMetaData != null )
        {
            result.putAll ( extractedMetaData );
        }

        // provided will override
        if ( providedMetaData != null )
        {
            result.putAll ( providedMetaData );
        }

        return Collections.unmodifiableMap ( result );
    }
}
