/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.api.upload;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import org.eclipse.packagedrone.web.util.Servlets;
import org.ops4j.pax.web.service.WebContainer;

public class ServletInitializer
{
    private WebContainer webContainer;

    private UploadServlet servlet;

    public void setWebContainer ( final WebContainer webContainer )
    {
        this.webContainer = webContainer;
    }

    public void start () throws ServletException
    {
        this.servlet = new UploadServlet ();
        final MultipartConfigElement mp = Servlets.createMultiPartConfiguration ( "drone.upload.servlet" );
        this.webContainer.registerServlet ( this.servlet, "upload", new String[] { "/api/v2/upload/*" }, null, 1, false, mp, null );

    }

    public void stop ()
    {
        this.webContainer.unregisterServlet ( this.servlet );
        this.servlet = null;
    }
}
