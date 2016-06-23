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
package org.eclipse.packagedrone.repo.api;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

@Path ( "/channels" )
@Api ( tags = { "Repository" } )
public interface Channels
{
    @GET
    @ApiOperation ( value = "List all channels" )
    @Produces ( MediaType.APPLICATION_JSON )
    @ApiResponses ( { @ApiResponse ( code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            response = ErrorInformation.class,
            message = "An internal error" ) } )
    public ChannelListResult list ();

    @PUT
    @ApiOperation ( value = "Create a new channel",
            authorizations = { @Authorization ( value = "access_token",
                    scopes = @AuthorizationScope ( scope = "MANAGER", description = "Manager the repository" ) ) } )
    @ApiResponses ( { @ApiResponse ( code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            response = ErrorInformation.class,
            message = "An internal error" ) } )
    @Produces ( MediaType.APPLICATION_JSON )
    @Path ( "/create" )
    @RolesAllowed ( "MANAGER" )
    public ChannelInformation createChannel ( @ApiParam ( required = true,
            value = "The new channel information" ) CreateChannel createChannel );
}
