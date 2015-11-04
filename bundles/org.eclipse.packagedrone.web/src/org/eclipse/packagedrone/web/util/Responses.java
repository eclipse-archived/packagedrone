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
package org.eclipse.packagedrone.web.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class Responses
{
    private Responses ()
    {
    }

    public static final String LAST_MODIFIED = "Last-Modified";

    public static void methodNotAllowed ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
    }

    public static void notFound ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        response.setCharacterEncoding ( "UTF-8" );
        response.setContentType ( "text/plain" );

        response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        response.getWriter ().println ( String.format ( "Resource '%s' could not be found", Requests.getOriginalPath ( request ) ) );
    }
}
