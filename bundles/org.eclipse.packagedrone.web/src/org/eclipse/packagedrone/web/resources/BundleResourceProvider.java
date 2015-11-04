/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.resources;

import java.net.URL;

import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.ResourceRequestHandler;
import org.osgi.framework.Bundle;

public class BundleResourceProvider implements ResourceHandlerProvider
{
    private final Bundle bundle;

    private final String resources;

    public BundleResourceProvider ( final Bundle bundle, final String resources )
    {
        this.bundle = bundle;
        this.resources = resources;
    }

    @Override
    public RequestHandler findHandler ( final String requestPath )
    {
        if ( !requestPath.startsWith ( this.resources ) )
        {
            return null;
        }

        final URL entry = this.bundle.getEntry ( requestPath );
        if ( entry != null )
        {
            return new ResourceRequestHandler ( entry, this.bundle.getLastModified () );
        }
        return null;
    }

}
