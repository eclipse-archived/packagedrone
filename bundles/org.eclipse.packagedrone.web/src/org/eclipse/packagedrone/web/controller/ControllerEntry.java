/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation;
import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerEntry
{

    private final static Logger logger = LoggerFactory.getLogger ( ControllerEntry.class );

    private final Object controller;

    private final Set<ControllerCall> calls = new HashSet<> ();

    private final ViewResolver viewResolver;

    public ControllerEntry ( final Object controller )
    {
        this.controller = controller;

        final Class<? extends Object> clazz = controller.getClass ();

        this.viewResolver = clazz.getAnnotation ( ViewResolver.class );

        final Set<ControllerInterceptorProcessor> interceptors = interceptorsFromController ();

        logger.debug ( "Interceptors for {}: {}", controller, interceptors );

        for ( final Method m : clazz.getMethods () )
        {
            final RequestMappingInformation rmi = parse ( m );

            if ( rmi != null )
            {
                this.calls.add ( new ControllerCall ( controller, rmi, m, interceptors ) );
            }
        }
    }

    private Set<ControllerInterceptorProcessor> interceptorsFromController ()
    {
        final ControllerInterceptor[] interceptors = this.controller.getClass ().getAnnotationsByType ( ControllerInterceptor.class );

        final Set<ControllerInterceptorProcessor> result = new HashSet<> ( interceptors.length );

        for ( final ControllerInterceptor inter : interceptors )
        {
            try
            {
                result.add ( inter.value ().newInstance () );
            }
            catch ( InstantiationException | IllegalAccessException e )
            {
                throw new RuntimeException ( e );
            }
        }

        return result;
    }

    public Object getController ()
    {
        return this.controller;
    }

    public ViewResolver getViewResolver ()
    {
        return this.viewResolver;
    }

    protected static RequestMappingInformation parse ( final Method method )
    {
        return Controllers.fromMethod ( method );
    }

    public RequestHandler findHandler ( final HttpServletRequest request, final HttpServletResponse response )
    {
        for ( final ControllerCall call : this.calls )
        {
            final Match match = call.matches ( request );
            if ( match == null )
            {
                continue;
            }

            final RequestHandler handler = call.call ( match, request, response );
            if ( handler != null )
            {
                return handler;
            }
        }
        return null;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[Controller: %s]", this.controller );
    }

}
