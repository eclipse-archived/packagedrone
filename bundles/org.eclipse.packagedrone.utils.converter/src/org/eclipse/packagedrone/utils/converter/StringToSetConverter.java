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
package org.eclipse.packagedrone.utils.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class StringToSetConverter implements Converter
{
    public static final StringToSetConverter INSTANCE = new StringToSetConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( ( from.equals ( String[].class ) || from.equals ( String.class ) ) && ( to.equals ( Set.class ) || to.equals ( SortedSet.class ) ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public SortedSet<?> convertTo ( final Object value, final Class<?> clazz )
    {
        if ( value == null )
        {
            return null;
        }

        if ( value instanceof String[] )
        {
            return convertFromArray ( (String[])value );
        }
        else
        {
            return convertFromValue ( value.toString () );
        }
    }

    private SortedSet<?> convertFromValue ( final String string )
    {
        return new TreeSet<> ( Collections.singleton ( string ) );
    }

    private SortedSet<?> convertFromArray ( final String[] value )
    {
        return new TreeSet<> ( Arrays.asList ( value ) );
    }
}
