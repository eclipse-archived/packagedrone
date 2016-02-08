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
package org.eclipse.packagedrone.repo;

import org.eclipse.packagedrone.utils.converter.ConversionException;
import org.eclipse.packagedrone.utils.converter.Converter;

public class StringToMetaKeyConverter implements Converter
{
    @Override
    public Object convertTo ( final Object value, final Class<?> clazz ) throws ConversionException
    {
        final String string = (String)value;

        if ( string.isEmpty () )
        {
            return null;
        }

        final MetaKey result = MetaKey.fromString ( string );
        if ( result == null )
        {
            throw new ConversionException ( String.format ( "Invalid meta key format, syntax is 'namespace:key'" ) );
        }
        return result;
    }

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        return to.equals ( MetaKey.class ) && from.equals ( String.class );
    }
}
