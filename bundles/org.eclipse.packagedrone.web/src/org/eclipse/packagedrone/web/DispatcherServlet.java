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
package org.eclipse.packagedrone.web;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;
import org.eclipse.packagedrone.web.controller.ControllerTracker;
import org.eclipse.packagedrone.web.interceptor.Interceptor;
import org.eclipse.packagedrone.web.interceptor.InterceptorLocator;
import org.eclipse.packagedrone.web.interceptor.InterceptorTracker;
import org.eclipse.packagedrone.web.resources.ResourceTracker;
import org.eclipse.packagedrone.web.util.Responses;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherServlet extends HttpServlet
{
    private final static Logger logger = LoggerFactory.getLogger ( DispatcherServlet.class );

    private static final long serialVersionUID = 1L;

    private RequestHandlerFactory resourceLocator;

    private RequestHandlerFactory controllerLocator;

    private InterceptorLocator interceptorLocator;

    private Optional<ErrorHandler> errorHandler = Optional.empty ();

    @Override
    public void init () throws ServletException
    {
        super.init ();

        final BundleContext context = FrameworkUtil.getBundle ( DispatcherServlet.class ).getBundleContext ();

        this.resourceLocator = new ResourceTracker ( context );
        this.controllerLocator = new ControllerTracker ( context );
        this.interceptorLocator = new InterceptorTracker ( context );
    }

    @Override
    public void destroy ()
    {
        if ( this.resourceLocator != null )
        {
            this.resourceLocator.close ();
            this.resourceLocator = null;
        }
        if ( this.controllerLocator != null )
        {
            this.controllerLocator.close ();
            this.controllerLocator = null;
        }
        if ( this.interceptorLocator != null )
        {
            this.interceptorLocator.close ();
            this.interceptorLocator = null;
        }
        super.destroy ();
    }

    public void setErrorHandler ( final ErrorHandler errorHandler )
    {
        this.errorHandler = Optional.ofNullable ( errorHandler );
    }

    @Override
    protected void service ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        logger.trace ( "service - {} - {} ({})", request.getMethod (), request.getServletPath (), request );

        try ( Handle handle = Profile.start ( makeOperationName ( request ) ) )
        {
            final Interceptor[] interceptors = this.interceptorLocator.getInterceptors ();

            runPreProcess ( interceptors, request, response );

            if ( response.isCommitted () )
            {
                return;
            }

            Exception ex = null;

            try
            {

                final RequestHandler requestHandler = mapRequest ( request, response );
                if ( requestHandler != null )
                {
                    runPostProcess ( interceptors, request, response, requestHandler );
                    requestHandler.process ( request, response );
                }
                else
                {
                    Responses.notFound ( request, response );
                }
            }
            catch ( final ServletException e )
            {
                throw e;
            }
            catch ( final Exception e )
            {
                ex = e;
                throw new ServletException ( e );
            }
            finally
            {
                runAfterCompletion ( interceptors, request, response, ex );
            }
        }
        catch ( final Exception e )
        {
            this.errorHandler.orElse ( ErrorHandler.DEFAULT ).handleError ( request, response, e );
        }
    }

    private static String makeOperationName ( final HttpServletRequest request )
    {
        return String.format ( "%s|%s", request.getRequestURI (), request.getMethod () );
    }

    protected void runAfterCompletion ( final Interceptor[] interceptors, final HttpServletRequest request, final HttpServletResponse response, final Exception ex ) throws Exception
    {
        for ( final Interceptor i : interceptors )
        {
            i.afterCompletion ( request, response, ex );
        }
    }

    protected void runPostProcess ( final Interceptor[] interceptors, final HttpServletRequest request, final HttpServletResponse response, final RequestHandler result ) throws Exception
    {
        for ( final Interceptor i : interceptors )
        {
            i.postHandle ( request, response, result );
        }
    }

    protected boolean runPreProcess ( final Interceptor[] interceptors, final HttpServletRequest request, final HttpServletResponse response ) throws Exception
    {
        for ( final Interceptor i : interceptors )
        {
            if ( !i.preHandle ( request, response ) )
            {
                return false;
            }
        }
        return true;
    }

    protected RequestHandler mapRequest ( final HttpServletRequest request, final HttpServletResponse response )
    {
        RequestHandler handler;

        handler = this.resourceLocator.handleRequest ( request, response );

        if ( handler != null )
        {
            return handler;
        }

        handler = this.controllerLocator.handleRequest ( request, response );
        if ( handler != null )
        {
            return handler;
        }

        return null;
    }

}
