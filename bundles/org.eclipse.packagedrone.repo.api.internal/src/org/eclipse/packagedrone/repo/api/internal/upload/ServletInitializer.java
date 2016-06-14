/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.api.internal.upload;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import org.eclipse.packagedrone.web.util.Servlets;
import org.ops4j.pax.web.service.WebContainer;

public class ServletInitializer
{
    private WebContainer webContainer;

    private UploadServletV2 uploadV2;

    private UploadServletV3 uploadV3;

    public void setWebContainer ( final WebContainer webContainer )
    {
        this.webContainer = webContainer;
    }

    public void start () throws ServletException
    {
        this.uploadV2 = new UploadServletV2 ();
        this.uploadV3 = new UploadServletV3 ();

        final MultipartConfigElement mp = Servlets.createMultiPartConfiguration ( "drone.upload.servlet" );

        this.webContainer.registerServlet ( this.uploadV2, "uploadV2", new String[] { "/api/v2/upload/*" }, null, 1, false, mp, null );
        this.webContainer.registerServlet ( this.uploadV3, "uploadV3", new String[] { UploadServletV3.BASE_PATH + "/*" }, null, 1, false, mp, null );
    }

    public void stop ()
    {
        this.webContainer.unregisterServlet ( this.uploadV2 );
        this.webContainer.unregisterServlet ( this.uploadV3 );

        this.uploadV2 = null;
        this.uploadV3 = null;
    }
}
