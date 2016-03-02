/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.eclipse.packagedrone.web.RequestHandler;

/**
 * Process a JAX RS {@link Response} object
 */
// TODO: forward this response to some sort of JAX RS implementation handler instead of doing it all by ourself
public class JaxRsResponseHandler implements RequestHandler
{
    private final Response response;

    public JaxRsResponseHandler ( final Response response )
    {
        this.response = Objects.requireNonNull ( response );
    }

    @Override
    public void process ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException
    {
        final MediaType mediaType = this.response.getMediaType ();

        if ( mediaType != null )
        {
            response.setContentType ( mediaType.toString () );
        }

        if ( this.response.getLength () > 0 )
        {
            response.setContentLength ( this.response.getLength () );
        }

        response.setStatus ( this.response.getStatus () );

        for ( final Map.Entry<String, List<String>> entry : this.response.getStringHeaders ().entrySet () )
        {
            for ( final String value : entry.getValue () )
            {
                response.addHeader ( entry.getKey (), value );
            }
        }

        if ( this.response.getCookies () != null )
        {
            for ( final Map.Entry<String, NewCookie> entry : this.response.getCookies ().entrySet () )
            {
                response.addCookie ( mapCookie ( entry ) );
            }
        }

        // the entity

        response.getWriter ().append ( this.response.getEntity ().toString () );
    }

    private static Cookie mapCookie ( final Map.Entry<String, NewCookie> entry )
    {
        final String name = entry.getKey ();
        final NewCookie nc = entry.getValue ();

        final Cookie cookie = new Cookie ( name, nc.getValue () );
        cookie.setComment ( nc.getComment () );
        cookie.setDomain ( nc.getDomain () );
        cookie.setHttpOnly ( nc.isHttpOnly () );
        cookie.setMaxAge ( nc.getMaxAge () );
        cookie.setPath ( nc.getPath () );
        cookie.setSecure ( nc.isSecure () );
        cookie.setVersion ( nc.getVersion () );
        return cookie;
    }

    @Override
    public void close ()
    {
        this.response.close ();
    }
}
