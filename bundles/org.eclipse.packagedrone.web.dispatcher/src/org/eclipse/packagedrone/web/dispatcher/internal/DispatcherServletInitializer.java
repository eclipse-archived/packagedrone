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
package org.eclipse.packagedrone.web.dispatcher.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.DispatcherServlet;
import org.eclipse.packagedrone.web.dispatcher.Dispatcher;
import org.eclipse.packagedrone.web.dispatcher.DispatcherHttpContext;
import org.eclipse.packagedrone.web.dispatcher.JspServletInitializer;
import org.eclipse.packagedrone.web.dispatcher.internal.internal.jsp.JspBundle;
import org.eclipse.packagedrone.web.dispatcher.internal.internal.jsp.JspBundleCustomizer;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.BundleTracker;

public class DispatcherServletInitializer
{
    public static class BundleFilter implements Filter
    {
        @Override
        public void destroy ()
        {
        }

        @Override
        public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException
        {
            if ( response instanceof HttpServletResponse )
            {
                ( (HttpServletResponse)response ).setStatus ( HttpServletResponse.SC_NOT_FOUND );
                response.setContentType ( "text/plain" );
                response.getWriter ().write ( "Resource not found" );
            }
        }

        @Override
        public void init ( final FilterConfig arg0 ) throws ServletException
        {

        }
    }

    private WebContainer webContainer;

    private BundleTracker<JspBundle> jspTracker;

    private FilterTracker proxyFilter;

    private DispatcherHttpContext context;

    public void setWebContainer ( final WebContainer webContainer )
    {
        this.webContainer = webContainer;
    }

    private static final String PROP_PREFIX = "osgi.web.dispatcher";

    private static final String PROP_PREFIX_MP = PROP_PREFIX + ".multipart";

    public void start () throws ServletException, NamespaceException
    {
        final BundleContext bundleContext = FrameworkUtil.getBundle ( DispatcherServletInitializer.class ).getBundleContext ();
        this.context = Dispatcher.createContext ( bundleContext );

        Dictionary<String, String> initparams = new Hashtable<> ();

        final MultipartConfigElement multipart = JspServletInitializer.createMultiPartConfiguration ( PROP_PREFIX_MP );
        this.webContainer.registerServlet ( new DispatcherServlet (), "dispatcher", new String[] { "/" }, initparams, 1, false, multipart, this.context );

        this.proxyFilter = new FilterTracker ( bundleContext );
        this.webContainer.registerFilter ( this.proxyFilter, new String[] { "/*" }, null, null, this.context );

        initparams = new Hashtable<> ();
        initparams.put ( "filter-mapping-dispatcher", "request" );
        this.webContainer.registerFilter ( new BundleFilter (), new String[] { "/bundle/*" }, null, initparams, this.context );

        this.jspTracker = new BundleTracker<> ( bundleContext, Bundle.INSTALLED | Bundle.ACTIVE, new JspBundleCustomizer ( this.webContainer, this.context ) );
        this.jspTracker.open ();
    }

    public void stop ()
    {
        this.context.dispose ();
        this.jspTracker.close ();
        this.webContainer.unregister ( "/" );
    }

}
