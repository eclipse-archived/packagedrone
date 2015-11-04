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
package org.eclipse.packagedrone.web.dispatcher.internal;

import java.net.URL;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleResourceProvider implements ResourceProvider
{
    private final static Logger logger = LoggerFactory.getLogger ( BundleResourceProvider.class );

    private final BundleContext context;

    public BundleResourceProvider ( final BundleContext context )
    {
        this.context = context;
    }

    @Override
    public URL getResource ( final String name )
    {
        if ( !name.startsWith ( "/bundle/" ) )
        {
            return null;
        }

        final String toks[] = name.split ( "/", 4 );

        if ( toks.length != 4 )
        {
            logger.debug ( "Invalid format: {}", new Object[] { toks } );
            return null;
        }

        if ( !toks[1].equals ( "bundle" ) )
        {
            return null;
        }

        final long bundleId;
        try
        {
            bundleId = Long.parseLong ( toks[2] );
        }
        catch ( final NumberFormatException e )
        {
            logger.debug ( "Failed to parse bundle id", e );
            return null;
        }

        final Bundle bundle = findBundle ( bundleId );
        logger.trace ( "Target bundle: {}", bundle );
        if ( bundle == null )
        {
            return null;
        }

        final URL result = bundle.getEntry ( toks[3] );
        logger.trace ( "Resource entry ({}): {}", toks[3], result );
        if ( result == null )
        {
            return null;
        }

        logger.debug ( "Requesting resource: {}", result );

        return result;
    }

    @Override
    public Set<String> getPaths ( final String path )
    {
        // we don't support browsing
        return null;
    }

    private Bundle findBundle ( final long bundleId )
    {
        return this.context.getBundle ( bundleId );
    }

}
