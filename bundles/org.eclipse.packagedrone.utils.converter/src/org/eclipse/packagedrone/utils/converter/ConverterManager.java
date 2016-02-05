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
package org.eclipse.packagedrone.utils.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.packagedrone.utils.converter.impl.BooleanToStringConverter;
import org.eclipse.packagedrone.utils.converter.impl.IntegerToStringConverter;
import org.eclipse.packagedrone.utils.converter.impl.JsonToStringConverter;
import org.eclipse.packagedrone.utils.converter.impl.LongToStringConverter;
import org.eclipse.packagedrone.utils.converter.impl.PrimitiveBooleanDefault;
import org.eclipse.packagedrone.utils.converter.impl.StringToBooleanConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToEnumConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToIntegerConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToJsonConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToLongConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToPrimitiveBooleanConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToPrimitiveIntegerConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToPrimitiveLongConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToSetConverter;

public class ConverterManager
{
    private final Collection<Converter> converters = new LinkedList<> ();

    private final Collection<DefaultProvider> defaults = new LinkedList<> ();

    public static ConverterManager create ()
    {
        final ConverterManager result = new ConverterManager ();

        result.addConverter ( StringToIntegerConverter.INSTANCE );
        result.addConverter ( StringToBooleanConverter.INSTANCE );
        result.addConverter ( StringToPrimitiveBooleanConverter.INSTANCE );
        result.addConverter ( StringToPrimitiveIntegerConverter.INSTANCE );
        result.addConverter ( StringToPrimitiveLongConverter.INSTANCE );
        result.addConverter ( BooleanToStringConverter.INSTANCE );
        result.addConverter ( IntegerToStringConverter.INSTANCE );
        result.addConverter ( StringToSetConverter.INSTANCE );
        result.addConverter ( StringToJsonConverter.INSTANCE );
        result.addConverter ( JsonToStringConverter.INSTANCE );
        result.addConverter ( LongToStringConverter.INSTANCE );
        result.addConverter ( StringToLongConverter.INSTANCE );

        result.addConverter ( StringToEnumConverter.INSTANCE );

        result.addDefault ( PrimitiveBooleanDefault.INSTANCE );

        return result;
    }

    public ConverterManager ()
    {
    }

    public void addConverter ( final Converter converter )
    {
        this.converters.add ( converter );
    }

    public void addDefault ( final DefaultProvider defaultProvider )
    {
        this.defaults.add ( defaultProvider );
    }

    public <T> T convertTo ( final Object value, final Class<T> clazz ) throws Exception
    {
        if ( value == null )
        {
            // process with default value
            return getDefault ( clazz );
        }

        if ( clazz.isAssignableFrom ( value.getClass () ) )
        {
            // is directly assignable ... no need to convert
            return clazz.cast ( value );
        }

        // proceed with conversion

        final Class<?> from = value.getClass ();

        ConversionResult<T> result;

        // try annotated converters

        final Collection<Converter> annotatedConverters = findAnnotatedConverters ( clazz );

        if ( annotatedConverters != null )
        {
            result = tryConvert ( value, from, clazz, annotatedConverters );
            if ( result != null )
            {
                return result.value;
            }
        }

        // try registered converters

        result = tryConvert ( value, from, clazz, this.converters );
        if ( result != null )
        {
            return result.value;
        }

        throw new ConversionException ( String.format ( "Unable to convert %s to %s", value.getClass (), clazz.getName () ) );
    }

    private static final class ConversionResult<T>
    {
        T value;

        public ConversionResult ( final T value )
        {
            this.value = value;
        }
    }

    @SuppressWarnings ( "unchecked" )
    private <T> ConversionResult<T> tryConvert ( final Object value, final Class<?> from, final Class<T> to, final Collection<Converter> converters )
    {
        for ( final Converter cvt : converters )
        {
            if ( cvt.canConvert ( from, to ) )
            {
                final Object o = cvt.convertTo ( value, to );
                if ( o == null )
                {
                    return new ConversionResult<> ( null );
                }

                if ( to.isAssignableFrom ( o.getClass () ) )
                {
                    return new ConversionResult<> ( to.cast ( o ) );
                }
                else if ( to.isPrimitive () )
                {
                    return new ConversionResult<> ( (T)o );
                }
                else
                {
                    throw new ConversionException ( String.format ( "Invalid result type (expected: %s, actual: %s)", to.getName (), o.getClass ().getName () ) );
                }
            }
        }
        return null;
    }

    private Collection<Converter> findAnnotatedConverters ( final Class<?> to )
    {
        final ConvertBy[] ann = to.getAnnotationsByType ( ConvertBy.class );
        if ( ann == null || ann.length <= 0 )
        {
            return null;
        }

        final ArrayList<Converter> result = new ArrayList<> ( ann.length );

        for ( final ConvertBy cb : ann )
        {
            try
            {
                result.add ( cb.value ().newInstance () );
            }
            catch ( InstantiationException | IllegalAccessException e )
            {
                // ignore
            }
        }

        return result;
    }

    @SuppressWarnings ( "unchecked" )
    private <T> T getDefault ( final Class<T> clazz )
    {
        for ( final DefaultProvider provider : this.defaults )
        {
            if ( provider.providesFor ( clazz ) )
            {
                return (T)provider.defaultValue ();
            }
        }

        // fall back to null
        return null;
    }
}
