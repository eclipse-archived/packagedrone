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

import com.google.gson.GsonBuilder;

public class JsonToStringConverter implements Converter
{
    public static final JsonToStringConverter INSTANCE = new JsonToStringConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        final boolean isJson = from.isAnnotationPresent ( JSON.class );
        if ( isJson && to.equals ( String.class ) )
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
        return new GsonBuilder ().create ().toJson ( value );
    }
}
