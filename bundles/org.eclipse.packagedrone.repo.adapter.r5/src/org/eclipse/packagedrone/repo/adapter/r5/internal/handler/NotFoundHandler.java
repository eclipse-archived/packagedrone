/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.r5.internal.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.servlet.Handler;

public class NotFoundHandler implements Handler
{
    private final String message;

    public NotFoundHandler ( final String message )
    {
        this.message = message;
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException
    {
        resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );

        final PrintWriter w = resp.getWriter ();
        resp.setContentType ( "text/plain" );

        w.println ( this.message );
    }

}
