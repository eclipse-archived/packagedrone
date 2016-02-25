/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.converter;

import java.lang.reflect.AnnotatedElement;

public interface Converter
{
    public Object convertTo ( Object value, Class<?> clazz, ConversionContext context ) throws ConversionException;

    public boolean canConvert ( Class<?> from, Class<?> to );

    public default boolean canConvert ( final Class<?> from, final Class<?> to, final AnnotatedElement annotatedElement )
    {
        return canConvert ( from, to );
    }
}
