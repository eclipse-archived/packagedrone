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

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.utils.converter.impl.BooleanToStringConverter;
import org.eclipse.packagedrone.utils.converter.impl.IntegerToStringConverter;
import org.eclipse.packagedrone.utils.converter.impl.JsonToStringConverter;
import org.eclipse.packagedrone.utils.converter.impl.LongToStringConverter;
import org.eclipse.packagedrone.utils.converter.impl.PrimitiveBooleanDefault;
import org.eclipse.packagedrone.utils.converter.impl.StringToArrayConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToBooleanConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToEnumConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToIntegerConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToJsonConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToLongConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToPrimitiveBooleanConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToPrimitiveIntegerConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToPrimitiveLongConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringToSetConverter;
import org.eclipse.packagedrone.utils.converter.impl.StringifyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConverterManager
{
    private final static Logger logger = LoggerFactory.getLogger ( ConverterManager.ConversionResult.class );

    private static final class ConversionResult<T>
    {
        T value;

        public ConversionResult ( final T value )
        {
            this.value = value;
        }
    }

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
        result.addConverter ( StringToArrayConverter.INSTANCE );
        result.addConverter ( StringifyConverter.INSTANCE );

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

    public <T> T convertTo ( final Object value, final Class<T> clazz ) throws ConversionException
    {
        return convertToByClass ( value, clazz, (Supplier<Collection<Class<? extends Converter>>>)null );
    }

    public <T> T convertTo ( final Object value, final Class<T> clazz, final AnnotatedElement targetElement ) throws ConversionException
    {
        return convertToByClass ( value, clazz, targetElement, () -> {
            final ConvertBy[] ann = targetElement.getAnnotationsByType ( ConvertBy.class );
            if ( ann != null && ann.length > 0 )
            {
                return Arrays.asList ( ann ).stream ().map ( ConvertBy::value ).collect ( Collectors.toList () );
            }
            return null;
        } );
    }

    public <T> T convertToBy ( final Object value, final Class<T> clazz, final Supplier<Collection<ConvertBy>> converterProvider ) throws ConversionException
    {
        return convertToByClass ( value, clazz, () -> {
            final Collection<ConvertBy> provider = converterProvider.get ();
            if ( provider == null || provider.isEmpty () )
            {
                return null;
            }

            return provider.stream ().map ( ConvertBy::value ).collect ( Collectors.toList () );
        } );
    }

    public <T> T convertToByClass ( final Object value, final Class<T> clazz, final Supplier<Collection<Class<? extends Converter>>> converterProvider ) throws ConversionException
    {
        return convertToByClass ( value, clazz, null, converterProvider );
    }

    public <T> T convertToByClass ( final Object value, final Class<T> clazz, final AnnotatedElement annotatedElement, final Supplier<Collection<Class<? extends Converter>>> converterProvider ) throws ConversionException
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

        // gather provided converters

        final Collection<Class<? extends Converter>> cvtClasses = converterProvider != null ? converterProvider.get () : null;

        final ConversionContext context = new ConversionContext () {

            @Override
            public <R> R convert ( final Object value, final Class<R> clazz ) throws ConversionException
            {
                return convertToByClass ( value, clazz, annotatedElement, () -> cvtClasses );
            }
        };

        ConversionResult<T> result;

        // try by provided converters

        if ( cvtClasses != null )
        {
            result = tryConvertByClass ( value, from, clazz, annotatedElement, cvtClasses, context );
            if ( result != null )
            {
                return result.value;
            }
        }

        // try annotated converters

        final Collection<Class<? extends Converter>> annotatedConverters = findAnnotatedConverters ( clazz );

        if ( annotatedConverters != null )
        {
            result = tryConvertByClass ( value, from, clazz, annotatedElement, annotatedConverters, context );
            if ( result != null )
            {
                return result.value;
            }
        }

        // try registered converters

        result = tryConvert ( value, from, clazz, annotatedElement, this.converters, context );
        if ( result != null )
        {
            return result.value;
        }

        throw new ConversionException ( String.format ( "Unable to convert %s to %s", value.getClass (), clazz.getName () ) );
    }

    private <T> ConversionResult<T> tryConvertByClass ( final Object value, final Class<?> from, final Class<T> to, final AnnotatedElement annotatedElement, final Collection<Class<? extends Converter>> converters, final ConversionContext context )
    {
        for ( final Class<? extends Converter> cvtClass : converters )
        {
            Converter cvt;
            try
            {
                cvt = cvtClass.newInstance ();
            }
            catch ( InstantiationException | IllegalAccessException e )
            {
                logger.warn ( "Failed to instantiate converter", e );
                continue;
            }
            final ConversionResult<T> result = tryConvert ( value, from, to, annotatedElement, cvt, context );
            if ( result != null )
            {
                return result;
            }
        }
        return null;
    }

    private <T> ConversionResult<T> tryConvert ( final Object value, final Class<?> from, final Class<T> to, final AnnotatedElement annotatedElement, final Collection<Converter> converters, final ConversionContext context )
    {
        for ( final Converter cvt : converters )
        {
            final ConversionResult<T> result = tryConvert ( value, from, to, annotatedElement, cvt, context );
            if ( result != null )
            {
                return result;
            }
        }
        return null;
    }

    @SuppressWarnings ( "unchecked" )
    private <T> ConversionResult<T> tryConvert ( final Object value, final Class<?> from, final Class<T> to, final AnnotatedElement annotatedElement, final Converter cvt, final ConversionContext context )
    {
        if ( !cvt.canConvert ( from, to, annotatedElement ) )
        {
            return null;
        }

        final Object o = cvt.convertTo ( value, to, context );
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

    private Collection<Class<? extends Converter>> findAnnotatedConverters ( final Class<?> to )
    {
        final ConvertBy[] ann = to.getAnnotationsByType ( ConvertBy.class );
        if ( ann == null || ann.length <= 0 )
        {
            return null;
        }

        final ArrayList<Class<? extends Converter>> result = new ArrayList<> ( ann.length );

        for ( final ConvertBy cb : ann )
        {
            result.add ( cb.value () );
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
