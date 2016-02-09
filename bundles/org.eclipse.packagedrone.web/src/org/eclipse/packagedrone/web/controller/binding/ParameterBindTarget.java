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
package org.eclipse.packagedrone.web.controller.binding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.packagedrone.utils.reflect.TypeResolver;

public class ParameterBindTarget implements BindTarget
{
    private final Parameter parameter;

    private final Object[] args;

    private final int argumentIndex;

    private final TypeResolver typeResolver;

    public ParameterBindTarget ( final Parameter parameter, final Object[] args, final int argumentIndex, final TypeResolver typeResolver )
    {
        this.parameter = parameter;
        this.args = args;
        this.argumentIndex = argumentIndex;
        this.typeResolver = typeResolver;
    }

    @Override
    public Class<?> getType ()
    {
        if ( this.typeResolver != null )
        {
            final Type result = this.typeResolver.resolveParameterType ( this.parameter );
            if ( result instanceof Class<?> )
            {
                return (Class<?>)result;
            }
        }
        return this.parameter.getType ();
    }

    @Override
    public String getQualifier ()
    {
        // although we do have a parameter name, it is not reliable
        return null;
    }

    @Override
    public boolean isAnnotationPresent ( final Class<? extends Annotation> clazz )
    {
        return this.parameter.isAnnotationPresent ( clazz );
    }

    @Override
    public <T extends Annotation> T getAnnotation ( final Class<T> clazz )
    {
        return this.parameter.getAnnotation ( clazz );
    }

    @Override
    public <T extends Annotation> Collection<T> getAnnotationsByType ( final Class<T> annotationClass )
    {
        return Arrays.asList ( this.parameter.getAnnotationsByType ( annotationClass ) );
    }

    @Override
    public void bind ( final Binding binding )
    {
        this.args[this.argumentIndex] = binding.getValue ();
    }

}
