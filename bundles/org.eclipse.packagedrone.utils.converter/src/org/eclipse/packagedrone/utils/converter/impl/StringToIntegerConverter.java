/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.converter.impl;

import org.eclipse.packagedrone.utils.converter.ConversionException;
import org.eclipse.packagedrone.utils.converter.Converter;

public class StringToIntegerConverter implements Converter
{
    public static final StringToIntegerConverter INSTANCE = new StringToIntegerConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( from.equals ( String.class ) && to.equals ( Integer.class ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public Integer convertTo ( final Object value, final Class<?> clazz )
    {
        if ( value == null )
        {
            return null;
        }

        try
        {
            final String str = value.toString ();
            if ( str.isEmpty () )
            {
                return null;
            }

            return Integer.parseInt ( value.toString () );
        }
        catch ( final NumberFormatException e )
        {
            throw new ConversionException ( String.format ( "'%s' is not a number", value ) );
        }
    }
}
