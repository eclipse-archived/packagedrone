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
package org.eclipse.packagedrone.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;

public class BundleResourceNotFoundRequestHandler implements RequestHandler
{
    private final Bundle bundle;

    private final String entryName;

    public BundleResourceNotFoundRequestHandler ( final Bundle bundle, final String entryName )
    {
        this.bundle = bundle;
        this.entryName = entryName;
    }

    @Override
    public void process ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException
    {
        response.sendError ( HttpServletResponse.SC_NOT_FOUND, makeMessage () );
    }

    private String makeMessage ()
    {
        return String.format ( "Resource '%s' could not be found in bundle %s", this.entryName, this.bundle.getSymbolicName () );
    }
}
