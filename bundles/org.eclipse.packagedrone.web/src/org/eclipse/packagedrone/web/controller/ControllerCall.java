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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.controller.binding.Binder;
import org.eclipse.packagedrone.web.controller.binding.BindingManager;
import org.eclipse.packagedrone.web.controller.binding.ErrorBinder;
import org.eclipse.packagedrone.web.controller.binding.PathVariableBinder;
import org.eclipse.packagedrone.web.controller.binding.RequestParameterBinder;
import org.eclipse.packagedrone.web.controller.form.FormDataBinder;
import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation;
import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation.Match;

public class ControllerCall
{
    private final RequestMappingInformation rmi;

    private final Method m;

    private final ControllerInterceptorProcessor[] interceptors;

    private final Object controller;

    public ControllerCall ( final Object controller, final RequestMappingInformation rmi, final Method m, final Set<ControllerInterceptorProcessor> interceptors )
    {
        this.controller = controller;
        this.rmi = rmi;
        this.m = m;
        this.interceptors = interceptors.toArray ( new ControllerInterceptorProcessor[interceptors.size ()] );
    }

    public Match matches ( final HttpServletRequest request )
    {
        return this.rmi.matches ( request );
    }

    protected RequestHandler runForward ( final int index, final HttpServletRequest request, final HttpServletResponse response, final Callable<RequestHandler> last ) throws Exception
    {
        if ( index < this.interceptors.length )
        {
            return this.interceptors[index].before ( this.controller, this.m, request, response, ( chainReq, chainRes ) -> {
                try
                {
                    return runForward ( index + 1, chainReq, chainRes, last );
                }
                catch ( final RuntimeException e )
                {
                    throw e;
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
            } );
        }
        else
        {
            return last.call ();
        }
    }

    protected RequestHandler runBackward ( final int index, final HttpServletRequest request, final HttpServletResponse response, final Callable<RequestHandler> last ) throws Exception
    {
        if ( index > 0 )
        {
            return this.interceptors[index - 1].after ( this.controller, this.m, request, response, ( chainReq, chainRes ) -> {
                try
                {
                    return runBackward ( index - 1, chainReq, chainRes, last );
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
            } );
        }
        else
        {
            return last.call ();
        }
    }

    public RequestHandler call ( final Match match, final HttpServletRequest request, final HttpServletResponse response )
    {
        // process call

        try
        {
            final RequestHandler result = runForward ( 0, request, response, () -> processCall ( match, request, response ) );

            return runBackward ( this.interceptors.length, request, response, new Callable<RequestHandler> () {

                @Override
                public RequestHandler call () throws Exception
                {
                    return result;
                }
            } );
        }
        catch ( final RuntimeException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    protected RequestHandler processCall ( final Match match, final HttpServletRequest request, final HttpServletResponse response ) throws Exception
    {
        final Map<String, Object> data = new HashMap<String, Object> ();

        data.put ( "request", request );
        data.put ( "response", response );
        data.put ( "session", request.getSession () );
        data.put ( "principal", request.getUserPrincipal () );

        // create new binding manager

        final BindingManager manager = BindingManager.create ( data );

        // add controller binders

        manager.addBinder ( new RequestParameterBinder ( request ) );
        manager.addBinder ( new PathVariableBinder ( match ) );
        manager.addBinder ( new FormDataBinder ( request, this.controller ) );

        addMethodBinders ( manager, this.m );

        final org.eclipse.packagedrone.web.controller.binding.BindingManager.Call call = manager.bind ( this.m, this.controller );
        final Object result = call.invoke ();

        if ( result instanceof ModelAndView )
        {
            return new ModelAndViewRequestHandler ( (ModelAndView)result, this.controller.getClass (), this.m );
        }
        else if ( result instanceof String )
        {
            return new ModelAndViewRequestHandler ( new ModelAndView ( (String)result ), this.controller.getClass (), this.m );
        }
        else if ( result == null )
        {
            return new NoOpRequestHandler ();
        }
        else
        {
            throw new IllegalStateException ( String.format ( "Response type %s is unsupported", result.getClass () ) );
        }

    }

    /**
     * Add custom binders assigned to the method
     * <p>
     * Custom binders assigned to the method will be added to the binding
     * manager instance.
     * </p>
     *
     * @param manager
     *            the manager to add binders to
     * @param method
     *            the method to evaluate for additional binders
     */
    protected static void addMethodBinders ( final BindingManager manager, final Method method )
    {
        final ControllerBinder[] binders = method.getAnnotationsByType ( ControllerBinder.class );

        if ( binders == null )
        {
            return;
        }

        for ( final ControllerBinder binder : binders )
        {
            try
            {
                final Binder binderImpl = binder.value ().newInstance ();
                if ( binderImpl instanceof ControllerBinderParametersAware )
                {
                    ( (ControllerBinderParametersAware)binderImpl ).setParameters ( binder.parameters () );
                }
                manager.addBinder ( binderImpl );
            }
            catch ( InstantiationException | IllegalAccessException e )
            {
                manager.addBinder ( new ErrorBinder ( e ) );
            }
        }
    }
}
