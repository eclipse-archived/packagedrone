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
package org.eclipse.packagedrone.sec.web.filter;

import java.security.Principal;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.eclipse.packagedrone.sec.UserInformation;
import org.eclipse.packagedrone.sec.UserInformationPrincipal;
import org.eclipse.packagedrone.sec.service.LoginException;
import org.eclipse.packagedrone.sec.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityHttpRequestWrapper extends HttpServletRequestWrapper
{
    private final static Logger logger = LoggerFactory.getLogger ( SecurityHttpRequestWrapper.class );

    private final SecurityService service;

    private final HttpServletRequest parent;

    public SecurityHttpRequestWrapper ( final SecurityService service, final HttpServletRequest request )
    {
        super ( request );
        this.parent = request;
        this.service = service;
    }

    @Override
    public void login ( final String username, final String password ) throws ServletException
    {
        final Object value = getSession ().getAttribute ( SecurityFilter.ATTR_USER_INFORMATION );
        if ( value != null )
        {
            logger.warn ( "Already logged in as: {}", value );
            throw new ServletException ( "Already logged in" );
        }

        try
        {
            if ( this.service == null )
            {
                throw new LoginException ( "No security service" );
            }

            final boolean rememberMe = Boolean.TRUE.equals ( getAttribute ( SecurityFilter.ATTR_REMEMBER_ME ) );

            final UserInformation user = this.service.login ( username, password, rememberMe );

            SecurityFilter.applyUserInformation ( this, user );
        }
        catch ( final Exception e )
        {
            throw new ServletException ( e );
        }
    }

    @Override
    public Principal getUserPrincipal ()
    {
        final UserInformation user = getUserDetails ( this.service, getSession ( false ) );
        if ( user == null )
        {
            return null;
        }

        return new UserInformationPrincipal ( user );
    }

    @Override
    public String getAuthType ()
    {
        final Principal user = getUserPrincipal ();
        if ( user != null )
        {
            return HttpServletRequest.FORM_AUTH;
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getRemoteUser ()
    {
        final Principal user = getUserPrincipal ();
        if ( user == null )
        {
            return null;
        }
        return user.getName ();
    }

    @Override
    public boolean isUserInRole ( final String role )
    {
        final UserInformation user = getUserDetails ( this.service, getSession ( false ) );

        if ( user == null )
        {
            return false;
        }

        final Set<String> roles = user.getRoles ();
        if ( roles == null )
        {
            return false;
        }

        return roles.contains ( role );
    }

    @Override
    public void logout () throws ServletException
    {
        getSession ().removeAttribute ( SecurityFilter.ATTR_USER_INFORMATION );
    }

    private static UserInformation getUserDetails ( final SecurityService service, final HttpSession session )
    {
        if ( session == null )
        {
            return null;
        }

        final Object user = session.getAttribute ( SecurityFilter.ATTR_USER_INFORMATION );
        if ( ! ( user instanceof UserInformation ) )
        {
            return null;
        }

        if ( session.getAttribute ( SecurityFilter.ATTR_USER_RELOAD ) != null )
        {
            session.removeAttribute ( SecurityFilter.ATTR_USER_RELOAD );

            // reload
            final UserInformation result = service.refresh ( (UserInformation)user );

            session.setAttribute ( SecurityFilter.ATTR_USER_INFORMATION, result );

            return result;
        }
        else
        {
            return (UserInformation)user;
        }
    }

    @Override
    public String toString ()
    {
        return String.format ( "[Security Wrapped Request: %s]", this.parent );
    }
}
