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

import org.eclipse.packagedrone.utils.converter.ConversionContext;
import org.eclipse.packagedrone.utils.converter.ConversionException;
import org.eclipse.packagedrone.utils.converter.Converter;

public class StringToEnumConverter implements Converter
{
    public static final Converter INSTANCE = new StringToEnumConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        return from.equals ( String.class ) && to.isEnum ();
    }

    @SuppressWarnings ( { "unchecked", "rawtypes" } )
    @Override
    public Object convertTo ( final Object value, final Class<?> clazz, final ConversionContext context ) throws ConversionException
    {
        final String string = (String)value;
        return Enum.valueOf ( (Class<? extends Enum>)clazz, string );
    }
}
