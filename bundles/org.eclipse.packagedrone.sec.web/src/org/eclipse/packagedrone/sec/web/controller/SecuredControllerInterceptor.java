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
package org.eclipse.packagedrone.sec.web.controller;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecuredControllerInterceptor extends AbstractSecurityControllerInterceptor
{

    private final static Logger logger = LoggerFactory.getLogger ( SecuredControllerInterceptor.class );

    @Override
    public RequestHandler before ( final Object controller, final Method m, final HttpServletRequest request, final HttpServletResponse response, final BiFunction<HttpServletRequest, HttpServletResponse, RequestHandler> next ) throws Exception
    {
        Secured s = m.getAnnotation ( Secured.class );
        if ( s == null )
        {
            s = controller.getClass ().getAnnotation ( Secured.class );
        }

        logger.trace ( "Checking secured: {} for {}", s, request );

        if ( s == null )
        {
            return next.apply ( request, response );
        }

        final Principal p = request.getUserPrincipal ();

        logger.trace ( "Principal: {}", p );

        if ( p == null && s.value () )
        {
            // anonymous - but not allowed

            return handleLoginRequired ( request, response );
        }

        return next.apply ( request, response );
    }
}
