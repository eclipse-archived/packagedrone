/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.util.Requests;
import org.eclipse.packagedrone.web.util.Responses;

import com.google.common.io.ByteStreams;

public class ResourceRequestHandler implements RequestHandler
{
    private final URL url;

    private final long lastModified;

    public ResourceRequestHandler ( final URL url, final long lastModified )
    {
        this.url = url;
        this.lastModified = lastModified;
    }

    @Override
    public void process ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        boolean isGet = request.getMethod ().equalsIgnoreCase ( "get" );

        if ( !isGet && request.getMethod ().equalsIgnoreCase ( "head" ) )
        {
            if ( Requests.isNotModified ( request, this.lastModified ) )
            {
                return;
            }
            isGet = true; // handle as GET
        }

        if ( !isGet )
        {
            Responses.methodNotAllowed ( request, response );
            return;
        }

        try ( InputStream in = this.url.openStream () )
        {
            response.setDateHeader ( Responses.LAST_MODIFIED, this.lastModified );
            ByteStreams.copy ( in, response.getOutputStream () );
        }
        catch ( final FileNotFoundException e )
        {
            // caused by accessing a directory
            Responses.notFound ( request, response );
        }

    }
}
