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

public class StringToPrimitiveBooleanConverter implements Converter
{
    public static final StringToPrimitiveBooleanConverter INSTANCE = new StringToPrimitiveBooleanConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( from.equals ( String.class ) && to.equals ( boolean.class ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public Object convertTo ( final Object value, final Class<?> clazz )
    {
        if ( value == null )
        {
            return null;
        }

        final String str = value.toString ();

        if ( "true".equalsIgnoreCase ( str ) )
        {
            return true;
        }
        if ( "on".equalsIgnoreCase ( str ) )
        {
            return true;
        }

        return false;
    }
}
