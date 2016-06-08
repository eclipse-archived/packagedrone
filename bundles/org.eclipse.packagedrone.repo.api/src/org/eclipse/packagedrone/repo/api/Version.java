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

import org.eclipse.packagedrone.VersionInformation;

@Path ( "/version" )
public class Version
{
    public static final class ProductVersion
    {
        private String productName;

        private String version;

        private String versionUnqualified;

        private String userAgent;

        private String buildId;

        public String getProductName ()
        {
            return this.productName;
        }

        public void setProductName ( final String productName )
        {
            this.productName = productName;
        }

        public void setVersion ( final String version )
        {
            this.version = version;
        }

        public String getVersion ()
        {
            return this.version;
        }

        public void setVersionUnqualified ( final String versionUnqualified )
        {
            this.versionUnqualified = versionUnqualified;
        }

        public String getVersionUnqualified ()
        {
            return this.versionUnqualified;
        }

        public void setUserAgent ( final String userAgent )
        {
            this.userAgent = userAgent;
        }

        public String getUserAgent ()
        {
            return this.userAgent;
        }

        public void setBuildId ( final String buildId )
        {
            this.buildId = buildId;
        }

        public String getBuildId ()
        {
            return this.buildId;
        }
    }

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

    @GET
    @Produces ( MediaType.APPLICATION_JSON )
    @Path ( "/product" )
    public ProductVersion productVersionJson ()
    {
        final ProductVersion result = new ProductVersion ();
        result.setProductName ( VersionInformation.PRODUCT );
        result.setVersion ( VersionInformation.VERSION );
        result.setVersionUnqualified ( VersionInformation.VERSION_UNQUALIFIED );
        result.setUserAgent ( VersionInformation.USER_AGENT );
        result.setBuildId ( VersionInformation.BUILD_ID.orElse ( null ) );
        return result;
    }
}
