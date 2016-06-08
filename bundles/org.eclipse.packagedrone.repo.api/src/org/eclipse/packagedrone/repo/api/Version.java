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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path ( "/version" )
public class Version
{
    /**
     * Get the version of the API as plain text
     *
     * @return the API version
     */
    @GET
    @Produces ( MediaType.TEXT_PLAIN )
    @Path ( "/api" )
    public String versionText ()
    {
        return "0.3";
    }
}
