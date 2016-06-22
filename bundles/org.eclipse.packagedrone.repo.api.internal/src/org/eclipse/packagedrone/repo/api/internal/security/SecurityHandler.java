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
package org.eclipse.packagedrone.repo.api.internal.security;

import java.security.Principal;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;

import org.eclipse.packagedrone.sec.service.SecurityService;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;
import com.eclipsesource.jaxrs.provider.security.AuthorizationHandler;

public class SecurityHandler implements AuthenticationHandler, AuthorizationHandler
{
    private SecurityService securityService;

    public void setSecurityService ( final SecurityService securityService )
    {
        this.securityService = securityService;
    }

    @Override
    public boolean isUserInRole ( final Principal user, final String role )
    {
        return false;
    }

    @Override
    public Principal authenticate ( final ContainerRequestContext requestContext )
    {
        final String auth = requestContext.getHeaderString ( "Authorization" );
        if ( auth != null && !auth.isEmpty () )
        {
            final String[] toks = auth.split ( "\\s+", 2 );
            if ( toks.length == 2 && toks[0].equals ( "token" ) )
            {
                final String token = toks[1];
                return this.securityService.accessByToken ( token ).orElse ( null );
            }
        }

        final List<String> tokens = requestContext.getUriInfo ().getQueryParameters ().get ( "access_token" );
        if ( tokens != null && !tokens.isEmpty () )
        {
            return this.securityService.accessByToken ( tokens.get ( 0 ) ).orElse ( null );
        }

        // nothing left
        return null;
    }

    @Override
    public String getAuthenticationScheme ()
    {
        return null;
    }

}
