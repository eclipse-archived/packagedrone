/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.converter.impl;

import org.eclipse.packagedrone.utils.converter.ConversionContext;
import org.eclipse.packagedrone.utils.converter.Converter;

public class IntegerToStringConverter implements Converter
{
    public static final IntegerToStringConverter INSTANCE = new IntegerToStringConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( from.equals ( Integer.class ) && to.equals ( String.class ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public String convertTo ( final Object value, final Class<?> clazz, final ConversionContext context )
    {
        if ( value == null )
        {
            return null;
        }
        return value.toString ();
    }
}
