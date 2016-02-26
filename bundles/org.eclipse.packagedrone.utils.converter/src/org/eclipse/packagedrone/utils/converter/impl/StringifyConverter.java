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
package org.eclipse.packagedrone.utils.converter.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.eclipse.packagedrone.utils.converter.ConversionContext;
import org.eclipse.packagedrone.utils.converter.ConversionException;
import org.eclipse.packagedrone.utils.converter.Converter;
import org.eclipse.packagedrone.utils.converter.Stringify;

public class StringifyConverter implements Converter
{
    public static final StringifyConverter INSTANCE = new StringifyConverter ();

    private StringifyConverter ()
    {
    }

    @Override
    public Object convertTo ( final Object value, final Class<?> clazz, final ConversionContext context ) throws ConversionException
    {
        if ( value instanceof String )
        {
            return fromString ( (String)value, clazz );
        }
        else if ( clazz.equals ( String.class ) )
        {
            return value.toString ();
        }

        // should never happen ;-)
        throw new ConversionException ( "Illegal combination for StringifyConverter" );
    }

    private Object fromString ( final String value, final Class<?> clazz ) throws ConversionException
    {
        try
        {
            final Method m = clazz.getDeclaredMethod ( "valueOf", String.class );
            return clazz.cast ( m.invoke ( null, value ) );
        }
        catch ( final Exception e )
        {
            throw new ConversionException ( e );
        }
    }

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        return canConvert ( from, to, null );
    }

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to, final AnnotatedElement annotatedElement )
    {
        if ( ! ( from.equals ( String.class ) || to.equals ( String.class ) ) )
        {
            // either one must be string
            return false;
        }

        if ( annotatedElement != null && annotatedElement.isAnnotationPresent ( Stringify.class ) )
        {
            return true;
        }

        if ( to.isAnnotationPresent ( Stringify.class ) )
        {
            return true;
        }

        return false;
    }

}
