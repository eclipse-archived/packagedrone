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
package org.eclipse.packagedrone.web.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BasicAuthentication
{
    private final static Logger logger = LoggerFactory.getLogger ( BasicAuthentication.class );

    private BasicAuthentication ()
    {
    }

    /**
     * Parse the basic authentication header
     *
     * @param request
     *            the request to fetch the header from
     * @return either <code>null</code> if no valid header entry was found, or a
     *         string array with exactly two entries (username, password)
     */
    public static String[] parseAuthorization ( final HttpServletRequest request )
    {
        final String auth = request.getHeader ( "Authorization" );
        logger.debug ( "Auth header: {}", auth );

        if ( auth == null || auth.isEmpty () )
        {
            return null;
        }

        final String[] toks = auth.split ( "\\s" );
        if ( toks.length < 2 )
        {
            return null;
        }

        if ( !"Basic".equalsIgnoreCase ( toks[0] ) )
        {
            return null;
        }

        final byte[] authData = Base64.getDecoder ().decode ( toks[1] );
        final String authStr = StandardCharsets.ISO_8859_1.decode ( ByteBuffer.wrap ( authData ) ).toString ();

        logger.debug ( "Auth String: {}", authStr );

        final String[] authToks = authStr.split ( ":", 2 );

        logger.debug ( "Auth tokens: {}", new Object[] { authToks } );

        if ( authToks.length != 2 )
        {
            return null;
        }

        return authToks;
    }

    public static void request ( final HttpServletResponse response, final String realm, final String message ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_UNAUTHORIZED );
        response.setHeader ( "WWW-Authenticate", String.format ( "Basic realm=\"%s\"", realm ) );

        response.getWriter ().write ( message );

    }

}
