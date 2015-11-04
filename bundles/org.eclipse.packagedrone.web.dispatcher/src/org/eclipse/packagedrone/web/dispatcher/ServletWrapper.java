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

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A simple wrapper around another servlet
 * <p>
 * This class does not provide any functionality other than to help wrapping
 * servlets.
 * </p>
 */
public class ServletWrapper implements Servlet
{
    private final Servlet servlet;

    public ServletWrapper ( final Servlet servlet )
    {
        this.servlet = servlet;
    }

    @Override
    public void destroy ()
    {
        this.servlet.destroy ();
    }

    @Override
    public ServletConfig getServletConfig ()
    {
        return this.servlet.getServletConfig ();
    }

    @Override
    public String getServletInfo ()
    {
        return this.servlet.getServletInfo ();
    }

    @Override
    public void init ( final ServletConfig config ) throws ServletException
    {
        this.servlet.init ( config );
    }

    @Override
    public void service ( final ServletRequest request, final ServletResponse response ) throws ServletException, IOException
    {
        this.servlet.service ( request, response );
    }

}
