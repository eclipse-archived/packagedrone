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
package org.eclipse.packagedrone.web.dispatcher.internal.internal.jsp;

import java.util.Enumeration;

import javax.servlet.ServletException;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JspBundleCustomizer implements BundleTrackerCustomizer<JspBundle>
{
    private final static Logger logger = LoggerFactory.getLogger ( JspBundleCustomizer.class );

    private final HttpService service;

    private final HttpContext context;

    public JspBundleCustomizer ( final HttpService service, final HttpContext context )
    {
        this.service = service;
        this.context = context;
    }

    @Override
    public JspBundle addingBundle ( final Bundle bundle, final BundleEvent event )
    {
        final Enumeration<String> result = bundle.getEntryPaths ( "/WEB-INF" );
        if ( result != null && result.hasMoreElements () )
        {
            try
            {
                return new JspBundle ( bundle, this.service, this.context );
            }
            catch ( ServletException | NamespaceException e )
            {
                logger.warn ( "Failed to register JSP bundle: " + bundle.getSymbolicName (), e );
                return null;
            }
        }

        return null;
    }

    @Override
    public void modifiedBundle ( final Bundle bundle, final BundleEvent event, final JspBundle jspBundle )
    {

    }

    @Override
    public void removedBundle ( final Bundle bundle, final BundleEvent event, final JspBundle jspBundle )
    {
        jspBundle.dispose ();
    }
}
