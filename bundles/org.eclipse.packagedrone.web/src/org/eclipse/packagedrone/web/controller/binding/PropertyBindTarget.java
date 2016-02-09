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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyBindTarget implements BindTarget
{
    private final static Logger logger = LoggerFactory.getLogger ( PropertyBindTarget.class );

    private final PropertyDescriptor propertyDescriptor;

    private final Class<?> objectClass;

    private final Object target;

    public PropertyBindTarget ( final Object target, final PropertyDescriptor propertyDescriptor )
    {
        this.propertyDescriptor = propertyDescriptor;
        this.target = target;
        this.objectClass = target.getClass ();
    }

    @Override
    public Class<?> getType ()
    {
        return this.propertyDescriptor.getPropertyType ();
    }

    @Override
    public String getQualifier ()
    {
        return this.propertyDescriptor.getName ();
    }

    @Override
    public boolean isAnnotationPresent ( final Class<? extends Annotation> clazz )
    {
        return getAnnotation ( clazz ) != null;
    }

    @Override
    public <T extends Annotation> Collection<T> getAnnotationsByType ( final Class<T> annotationClass )
    {
        final Collection<T> result = new LinkedList<> ();

        result.addAll ( Arrays.asList ( this.propertyDescriptor.getWriteMethod ().getAnnotationsByType ( annotationClass ) ) );

        Field field;
        try
        {
            field = this.objectClass.getField ( this.propertyDescriptor.getName () );
            result.addAll ( Arrays.asList ( field.getAnnotationsByType ( annotationClass ) ) );
        }
        catch ( final Exception e )
        {
            return null;
        }

        return result;
    }

    @Override
    public <T extends Annotation> T getAnnotation ( final Class<T> clazz )
    {
        final T a = this.propertyDescriptor.getWriteMethod ().getAnnotation ( clazz );
        if ( a != null )
        {
            return a;
        }

        Field field;
        try
        {
            field = this.objectClass.getField ( this.propertyDescriptor.getName () );
        }
        catch ( final Exception e )
        {
            return null;
        }

        return field.getAnnotation ( clazz );
    }

    @Override
    public void bind ( final Binding binding )
    {
        try
        {
            this.propertyDescriptor.getWriteMethod ().invoke ( this.target, binding.getValue () );
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to apply property", e );
            binding.getBindingResult ().addError ( new ExceptionError ( e ) );
        }
    }
}
