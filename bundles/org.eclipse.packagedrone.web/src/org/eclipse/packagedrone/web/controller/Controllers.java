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
package org.eclipse.packagedrone.web.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation;

public final class Controllers
{
    public static RequestMappingInformation fromMethod ( final Class<?> controllerClazz, final Method method )
    {
        final RequestMapping methodAn = method.getAnnotation ( RequestMapping.class );
        final RequestMapping classAn = getAnnotation ( controllerClazz, RequestMapping.class );

        if ( methodAn == null )
        {
            return null;
        }

        final Set<String> httpMethods = findHttpMethods ( classAn, methodAn );
        final Set<String> paths = expandPaths ( classAn, methodAn );

        if ( httpMethods.isEmpty () || paths.isEmpty () )
        {
            // if we don't have a method or path, then we don't handle requests
            return null;
        }

        return new RequestMappingInformation ( paths, httpMethods );
    }

    private static Set<String> expandPaths ( final RequestMapping classAn, final RequestMapping methodAn )
    {
        final Set<String> classPaths = toSet ( classAn );
        final Set<String> methodPaths = toSet ( methodAn );

        if ( classPaths.isEmpty () )
        {
            return methodPaths;
        }
        if ( methodPaths.isEmpty () )
        {
            return classPaths;
        }

        // merge

        final Set<String> result = new HashSet<> ();

        for ( final String base : classPaths )
        {
            for ( final String p : methodPaths )
            {
                result.add ( base + p );
            }
        }

        return result;
    }

    protected static Set<String> toSet ( final RequestMapping classAn )
    {
        if ( classAn != null && classAn.value () != null && classAn.value ().length > 0 )
        {
            return new HashSet<> ( Arrays.asList ( classAn.value () ) );
        }
        else
        {
            return Collections.emptySet ();
        }
    }

    private static Set<String> findHttpMethods ( final RequestMapping classAn, final RequestMapping methodAn )
    {
        if ( methodAn != null && methodAn.method () != null && methodAn.method ().length > 0 )
        {
            return toSet ( methodAn.method () );
        }

        if ( classAn != null && classAn.method () != null && classAn.method ().length > 0 )
        {
            return toSet ( classAn.method () );
        }

        return Collections.emptySet ();
    }

    private static Set<String> toSet ( final RequestMethod[] methods )
    {
        final Set<String> result = new HashSet<> ( methods.length );
        for ( final RequestMethod method : methods )
        {
            result.add ( method.name () );
        }
        return result;
    }

    private static <A extends Annotation> A getAnnotation ( final Class<?> clazz, final Class<A> annotationClass )
    {
        return clazz.getAnnotation ( annotationClass );
    }

}
