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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.ViewResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelAndViewRequestHandler implements RequestHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( ModelAndViewRequestHandler.class );

    private final ModelAndView modelAndView;

    private final Class<?> controllerClazz;

    private final Method method;

    public ModelAndViewRequestHandler ( final ModelAndView modelAndView, final Class<?> controllerClazz, final Method method )
    {
        this.modelAndView = modelAndView;
        this.controllerClazz = controllerClazz;
        this.method = method;
    }

    public ModelAndView getModelAndView ()
    {
        return this.modelAndView;
    }

    @Override
    public void process ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException
    {
        final String redir = this.modelAndView.getRedirect ();
        if ( redir != null )
        {
            logger.debug ( "Processed redirect: {}", redir );
            response.sendRedirect ( redir );
            return;
        }

        if ( this.modelAndView.isReferer () )
        {
            final String ref = request.getHeader ( "Referer" );

            if ( ref == null )
            {
                response.sendRedirect ( this.modelAndView.getReferer () );
            }

            final URL url = new URL ( ref );
            String path = url.getPath ();
            if ( path.startsWith ( request.getContextPath () ) )
            {
                path = path.substring ( request.getContextPath ().length () );
            }

            response.sendRedirect ( path );
            return;
        }

        ViewResolver viewResolver = null;
        Class<?> resourceClazz = this.controllerClazz;

        if ( this.modelAndView.getAlternateViewResolver () != null )
        {
            viewResolver = this.modelAndView.getAlternateViewResolver ().getAnnotation ( ViewResolver.class );
            resourceClazz = this.modelAndView.getAlternateViewResolver ();
        }

        if ( viewResolver == null && this.method != null )
        {
            viewResolver = this.method.getAnnotation ( ViewResolver.class );
        }

        if ( viewResolver == null && this.controllerClazz != null )
        {
            viewResolver = this.controllerClazz.getAnnotation ( ViewResolver.class );
        }

        if ( viewResolver == null )
        {
            throw new IllegalStateException ( String.format ( "View resolver for %s not declared. Missing @%s annotation?", this.controllerClazz.getName (), ViewResolver.class.getSimpleName () ) );
        }

        final Bundle bundle = FrameworkUtil.getBundle ( resourceClazz );

        final StringBuilder pathBuilder = new StringBuilder ( "/bundle/" );
        pathBuilder.append ( bundle.getBundleId () );

        final String fullViewName = String.format ( viewResolver.value (), this.modelAndView.getViewName () );

        if ( !fullViewName.startsWith ( "/" ) )
        {
            pathBuilder.append ( '/' );
        }
        pathBuilder.append ( fullViewName );

        final String path = normalize ( pathBuilder.toString () );

        logger.debug ( "Render: {}", path );

        setModelAsRequestAttributes ( request, this.modelAndView.getModel () );

        try ( Handle handle = Profile.start ( "render:" + this.modelAndView.getViewName () ) )
        {
            render ( request, response, path );
        }
    }

    private static String normalize ( final String string )
    {
        final int len = string.length ();

        final StringBuilder sb = new StringBuilder ( len );

        boolean slash = false;
        for ( int i = 0; i < len; i++ )
        {
            final char c = string.charAt ( i );
            if ( c == '/' )
            {
                if ( !slash )
                {
                    slash = true;
                    sb.append ( c );
                }
            }
            else
            {
                sb.append ( c );
                slash = false;
            }
        }

        return sb.toString ();
    }

    private void render ( final HttpServletRequest request, final HttpServletResponse response, final String path ) throws ServletException, IOException
    {
        final RequestDispatcher rd = request.getRequestDispatcher ( path );
        if ( response.isCommitted () )
        {
            logger.trace ( "Including" );
            rd.include ( request, response );
        }
        else
        {
            logger.trace ( "Forwarding" );
            rd.forward ( request, response );
        }
    }

    private void setModelAsRequestAttributes ( final HttpServletRequest request, final Map<String, Object> model )
    {
        for ( final Map.Entry<String, Object> entry : model.entrySet () )
        {
            request.setAttribute ( entry.getKey (), entry.getValue () );
        }
    }

    @Override
    public String toString ()
    {
        return String.format ( "[RequestHandler/ModelAndView - %s]", this.modelAndView );
    }
}
