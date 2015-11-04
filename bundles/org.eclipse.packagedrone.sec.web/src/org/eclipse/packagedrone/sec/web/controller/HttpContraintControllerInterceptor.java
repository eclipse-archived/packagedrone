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

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpContraintControllerInterceptor extends AbstractSecurityControllerInterceptor
{
    private final static Logger logger = LoggerFactory.getLogger ( HttpContraintControllerInterceptor.class );

    @Override
    public RequestHandler before ( final Object controller, final Method m, final HttpServletRequest request, final HttpServletResponse response, final BiFunction<HttpServletRequest, HttpServletResponse, RequestHandler> next ) throws Exception
    {
        HttpConstraint s = m.getAnnotation ( HttpConstraint.class );
        if ( s == null )
        {
            s = controller.getClass ().getAnnotation ( HttpConstraint.class );
        }

        logger.trace ( "Checking http contraints: {} for {}", s, request );

        if ( s == null )
        {
            return next.apply ( request, response );
        }

        if ( isAllowed ( s, request ) )
        {
            return next.apply ( request, response );
        }

        final Principal p = request.getUserPrincipal ();
        if ( p == null )
        {
            // make a different when no one is logged in
            return handleLoginRequired ( request, response );
        }

        return handleAccessDenied ( response );
    }

    public static boolean isAllowed ( final HttpConstraint constraint, final HttpServletRequest request )
    {
        final EmptyRoleSemantic empty = constraint.value ();
        final String[] allowedRoles = constraint.rolesAllowed ();

        if ( allowedRoles == null || allowedRoles.length <= 0 )
        {
            // no roles
            if ( EmptyRoleSemantic.PERMIT.equals ( empty ) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            // check all roles .. one is ok

            for ( final String role : allowedRoles )
            {
                if ( request.isUserInRole ( role ) )
                {
                    return true;
                }
            }

            // we ran out of options

            return false;
        }
    }
}
