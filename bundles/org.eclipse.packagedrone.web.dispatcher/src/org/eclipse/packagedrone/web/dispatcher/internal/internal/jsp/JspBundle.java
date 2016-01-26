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

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;

import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JspBundle
{
    private final static Logger logger = LoggerFactory.getLogger ( JspBundle.class );

    private final String alias;

    private final JspServlet servlet;

    private final HttpService service;

    public JspBundle ( final Bundle bundle, final HttpService service, final HttpContext context ) throws ServletException, NamespaceException
    {
        this.service = service;

        this.alias = String.format ( "/bundle/%s/WEB-INF", bundle.getBundleId () );
        this.servlet = new JspServlet ( bundle, "/WEB-INF", this.alias );

        logger.info ( "Registering JSP servlet - resources: /WEB-INF, alias: {}, bundle: {}", this.alias, bundle.getSymbolicName () );

        final Dictionary<String, String> initparams = new Hashtable<> ( 2 );
        initparams.put ( "compilerSourceVM", "1.8" );
        initparams.put ( "compilerTargetVM", "1.8" );

        this.service.registerServlet ( this.alias, this.servlet, initparams, context );
    }

    public void dispose ()
    {
        logger.info ( "Unregistering JSP servlet - alias: {}", this.alias );
        this.service.unregister ( this.alias );
    }
}
