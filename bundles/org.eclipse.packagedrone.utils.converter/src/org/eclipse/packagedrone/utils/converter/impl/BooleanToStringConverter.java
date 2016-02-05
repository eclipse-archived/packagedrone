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
package org.eclipse.packagedrone.utils.converter.impl;

import org.eclipse.packagedrone.utils.converter.Converter;

public class BooleanToStringConverter implements Converter
{
    public static final BooleanToStringConverter INSTANCE = new BooleanToStringConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( from.equals ( Boolean.class ) && to.equals ( String.class ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public String convertTo ( final Object value, final Class<?> clazz )
    {
        if ( value == null )
        {
            return null;
        }
        return value.toString ();
    }
}
