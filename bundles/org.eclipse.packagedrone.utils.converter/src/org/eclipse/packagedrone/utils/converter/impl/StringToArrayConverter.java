/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.converter.impl;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.packagedrone.utils.converter.ConversionContext;
import org.eclipse.packagedrone.utils.converter.ConversionException;
import org.eclipse.packagedrone.utils.converter.Converter;

public class StringToArrayConverter implements Converter
{
    public static final StringToArrayConverter INSTANCE = new StringToArrayConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( !to.isArray () )
        {
            return false;
        }

        final Class<?> compClass = to.getComponentType ();
        if ( compClass == null )
        {
            return false;
        }

        return true;
    }

    @Override
    public Object convertTo ( final Object value, final Class<?> clazz, final ConversionContext context )
    {
        if ( value == null )
        {
            return null;
        }

        final Class<?> compClass = clazz.getComponentType ();

        List<String> input;
        if ( value instanceof String[] )
        {
            input = Arrays.asList ( (String[])value );
        }
        else
        {
            input = Collections.singletonList ( (String)value );
        }

        return convertToComponent ( input, compClass, context );
    }

    private Object convertToComponent ( final List<String> input, final Class<?> compClass, final ConversionContext context ) throws ConversionException
    {
        final Object result = Array.newInstance ( compClass, input.size () );

        int i = 0;
        for ( final String tok : input )
        {
            try
            {
                Array.set ( result, i, context.convert ( tok, compClass ) );
            }
            catch ( final Exception e )
            {
                throw new ConversionException ( e );
            }
            i++;
        }

        return result;
    }

}
