/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.dispatcher;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.service.http.NamespaceException;

/**
 * Register a servlet and add JSP support
 * <p>
 * <em>Note:</em> All JSPs must be located under <code>/WEB-INF/views</code>
 * </p>
 */
public abstract class JspServletInitializer
{
    public static class BundleServletWrapper extends ServletWrapper
    {
        private final Bundle bundle;

        public BundleServletWrapper ( final Servlet servlet, final Bundle bundle )
        {
            super ( servlet );
            this.bundle = bundle;
        }

        @Override
        public void service ( final ServletRequest request, final ServletResponse response ) throws ServletException, IOException
        {
            if ( request instanceof HttpServletRequest )
            {
                super.service ( new BundleRequestWrapper ( (HttpServletRequest)request, this.bundle ), response );
            }
            else
            {
                super.service ( request, response );
            }
        }
    }

    public static class BundleRequestWrapper extends HttpServletRequestWrapper
    {
        private final Bundle bundle;

        public BundleRequestWrapper ( final HttpServletRequest request, final Bundle bundle )
        {
            super ( request );
            this.bundle = bundle;
        }

        @Override
        public RequestDispatcher getRequestDispatcher ( String path )
        {
            if ( path != null && path.startsWith ( "/WEB-INF" ) )
            {
                path = String.format ( "/bundle/%d%s", this.bundle.getBundleId (), path );
            }
            return super.getRequestDispatcher ( path );
        }
    }

    private WebContainer httpService;

    private DispatcherHttpContext webctx;

    private JspServlet jspServlet;

    private final String alias;

    private Servlet servlet;

    private final Bundle bundle;

    protected abstract Servlet createServlet ();

    public JspServletInitializer ( final String alias, final Bundle bundle )
    {
        this.alias = !alias.endsWith ( "/" ) ? alias : alias.substring ( 0, alias.length () - 1 );
        this.bundle = bundle;
    }

    public void setHttpService ( final WebContainer httpService )
    {
        this.httpService = httpService;
    }

    public void start () throws ServletException, NamespaceException
    {
        this.webctx = Dispatcher.createContext ( this.bundle.getBundleContext () );

        this.servlet = new BundleServletWrapper ( createServlet (), this.bundle );

        this.httpService.registerServlet ( this.alias, this.servlet, null, this.webctx );

        this.jspServlet = new JspServlet ( this.bundle, "/WEB-INF/views", String.format ( "/bundle/%d/WEB-INF/views", this.bundle.getBundleId () ) );
        this.httpService.registerServlet ( this.jspServlet, new String[] { "*.jsp" }, null, this.webctx );
    }

    public void stop ()
    {
        this.httpService.unregisterServlet ( this.jspServlet );
        this.httpService.unregisterServlet ( this.servlet );

        this.webctx.dispose ();
    }
}
