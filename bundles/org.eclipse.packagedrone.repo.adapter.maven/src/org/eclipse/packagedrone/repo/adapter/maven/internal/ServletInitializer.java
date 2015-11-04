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
package org.eclipse.packagedrone.repo.adapter.maven.internal;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import org.eclipse.packagedrone.web.dispatcher.JspServletInitializer;
import org.ops4j.pax.web.service.WebContainer;

public class ServletInitializer
{
    private WebContainer webContainer;

    private MavenServlet servlet;

    public void setWebContainer ( final WebContainer webContainer )
    {
        this.webContainer = webContainer;
    }

    public void start () throws ServletException
    {
        this.servlet = new MavenServlet ();

        final MultipartConfigElement mp = JspServletInitializer.createMultiPartConfiguration ( "drone.maven.servlet" );
        this.webContainer.registerServlet ( this.servlet, "maven", new String[] { "/maven/*" }, null, 1, false, mp, null );
    }

    public void stop ()
    {
        this.webContainer.unregisterServlet ( this.servlet );
        this.servlet = null;
    }
}
