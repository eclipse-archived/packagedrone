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

import java.lang.reflect.AnnotatedElement;

import org.eclipse.packagedrone.utils.converter.ConversionContext;
import org.eclipse.packagedrone.utils.converter.Converter;
import org.eclipse.packagedrone.utils.converter.JSON;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StringToJsonConverter implements Converter
{
    public static final StringToJsonConverter INSTANCE = new StringToJsonConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        return canConvert ( from, to, null );
    }

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to, final AnnotatedElement annotatedElement )
    {
        if ( !from.equals ( String.class ) )
        {
            return false;
        }

        if ( to.isAnnotationPresent ( JSON.class ) )
        {
            return true;
        }

        if ( annotatedElement != null && annotatedElement.isAnnotationPresent ( JSON.class ) )
        {
            return true;
        }

        return false;
    }

    @Override
    public Object convertTo ( final Object value, final Class<?> clazz, final ConversionContext context )
    {
        if ( value == null )
        {
            return null;
        }

        final String val = (String)value;

        final Gson gson = new GsonBuilder ().create ();
        return gson.fromJson ( val, clazz );
    }
}
