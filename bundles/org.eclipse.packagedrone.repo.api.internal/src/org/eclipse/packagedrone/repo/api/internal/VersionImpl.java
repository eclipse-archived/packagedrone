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
package org.eclipse.packagedrone.repo.api.internal;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.repo.api.Version;

public class VersionImpl implements Version
{
    public static final String API_VERSION = "0.3";

    /**
     * Get the version of the API as plain text
     *
     * @return the API version
     */
    @Override
    public String versionText ()
    {
        return API_VERSION;
    }

    @Override
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
