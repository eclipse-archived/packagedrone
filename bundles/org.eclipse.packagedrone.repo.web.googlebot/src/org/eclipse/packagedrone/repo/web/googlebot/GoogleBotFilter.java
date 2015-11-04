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
package org.eclipse.packagedrone.repo.web.googlebot;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GoogleBotFilter implements Filter
{
    private static final String USER_AGENT_HEADER_NAME = "User-Agent";

    private static final String GOOGLE_BOT_FRAGMENT = System.getProperty ( "drone.web.googlebot.fragment", "googlebot" ).toLowerCase ();

    @Override
    public void init ( final FilterConfig config ) throws ServletException
    {
    }

    @Override
    public void destroy ()
    {
    }

    @Override
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException
    {
        if ( isGoogleBot ( request ) && response instanceof HttpServletResponse )
        {
            chain.doFilter ( request, new GoogleBotWrapper ( (HttpServletResponse)response ) );
        }
        else
        {
            chain.doFilter ( request, response );
        }
    }

    protected static boolean isGoogleBot ( final ServletRequest request )
    {
        if ( request instanceof HttpServletRequest )
        {
            final HttpServletRequest httpRequest = (HttpServletRequest)request;

            final Enumeration<String> headers = httpRequest.getHeaders ( USER_AGENT_HEADER_NAME );
            while ( headers.hasMoreElements () )
            {
                final String header = headers.nextElement ();
                if ( header == null )
                {
                    continue;
                }

                if ( header.toLowerCase ().contains ( GOOGLE_BOT_FRAGMENT ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

}
