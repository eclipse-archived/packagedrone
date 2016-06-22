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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;

@Path ( "/version" )
@Api
public interface Version
{
    @ApiModel ( description = "Information about the product version" )
    public static final class ProductVersion
    {
        private String productName;

        private String version;

        private String versionUnqualified;

        private String userAgent;

        private String buildId;

        public void setProductName ( final String productName )
        {
            this.productName = productName;
        }

        @ApiModelProperty ( "The name of the product" )
        public String getProductName ()
        {
            return this.productName;
        }

        public void setVersion ( final String version )
        {
            this.version = version;
        }

        @ApiModelProperty ( "The qualified version" )
        public String getVersion ()
        {
            return this.version;
        }

        public void setVersionUnqualified ( final String versionUnqualified )
        {
            this.versionUnqualified = versionUnqualified;
        }

        @ApiModelProperty ( "The unqualified version" )
        public String getVersionUnqualified ()
        {
            return this.versionUnqualified;
        }

        public void setUserAgent ( final String userAgent )
        {
            this.userAgent = userAgent;
        }

        @ApiModelProperty ( "The user agent string" )
        public String getUserAgent ()
        {
            return this.userAgent;
        }

        public void setBuildId ( final String buildId )
        {
            this.buildId = buildId;
        }

        @ApiModelProperty ( "The build id" )
        public String getBuildId ()
        {
            return this.buildId;
        }
    }

    public static final String API_VERSION = "0.3";

    /**
     * Get the version of the API as plain text
     *
     * @return the API version
     */
    @GET
    @Produces ( MediaType.TEXT_PLAIN )
    @Path ( "/api" )
    @ApiOperation ( value = "Get the version of the API", notes = "This returns the version of the API" )
    public String versionText ();

    @GET
    @Produces ( MediaType.APPLICATION_JSON )
    @Path ( "/product" )
    @ApiOperation ( value = "Get the different version informations of this instance",
            notes = "Fetch all different kind of version information about this installation" )
    public ProductVersion productVersionJson ();
}
