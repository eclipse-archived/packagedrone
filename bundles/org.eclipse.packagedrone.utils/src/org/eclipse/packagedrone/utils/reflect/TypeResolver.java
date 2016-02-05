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
package org.eclipse.packagedrone.utils.reflect;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class TypeResolver
{
    private final Class<?> clazz;

    private Map<TypeVariable<?>, Type> cache;

    public TypeResolver ( final Class<?> contextClass )
    {
        this.clazz = contextClass;
    }

    public Type resolveParameterType ( final Parameter parameter )
    {
        return resolveType ( parameter.getParameterizedType () );
    }

    public Type resolveType ( final Type type )
    {
        if ( type instanceof TypeVariable<?> )
        {
            return resolve ( (TypeVariable<?>)type );
        }
        else
        {
            return type;
        }
    }

    private Type resolve ( final TypeVariable<?> typeVariable )
    {
        if ( this.cache == null )
        {
            this.cache = buildTypeVariableMap ( this.clazz );
        }
        return this.cache.get ( typeVariable );
    }

    private static Map<TypeVariable<?>, Type> buildTypeVariableMap ( final Class<?> clazz )
    {
        final Map<TypeVariable<?>, Type> result = new HashMap<> ();

        fillFrom ( clazz, result );

        return result;
    }

    private static void fillFrom ( final Class<?> clazz, final Map<TypeVariable<?>, Type> result )
    {
        fill ( result, clazz.getGenericSuperclass () );
        for ( final Type genericClass : clazz.getGenericInterfaces () )
        {
            fill ( result, genericClass );
        }
    }

    private static void fill ( final Map<TypeVariable<?>, Type> result, final Type genericClass )
    {
        if ( genericClass instanceof Class<?> )
        {
            fillFrom ( (Class<?>)genericClass, result );
        }
        else if ( genericClass instanceof ParameterizedType )
        {
            final ParameterizedType pt = (ParameterizedType)genericClass;
            final Type rt = pt.getRawType ();

            if ( rt instanceof Class<?> )
            {
                final Class<?> rtc = (Class<?>)rt;

                final TypeVariable<?>[] tp = rtc.getTypeParameters ();
                final Type[] atp = pt.getActualTypeArguments ();

                for ( int i = 0; i < tp.length; i++ )
                {
                    Type at = atp[i];
                    if ( at instanceof TypeVariable<?> )
                    {
                        at = result.get ( at );
                    }
                    result.put ( tp[i], at );
                }

                fillFrom ( rtc, result );
            }
        }
    }

}
