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
package org.eclipse.packagedrone.addon.internal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.packagedrone.addon.AddonManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator
{
    private final static Logger logger = LoggerFactory.getLogger ( Activator.class );

    private AddonManagerImpl manager;

    private ServiceRegistration<AddonManager> handle;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        final Path path = initPath ( context );
        if ( path != null )
        {
            this.manager = new AddonManagerImpl ( path );
            final Dictionary<String, Object> properties = new Hashtable<> ();
            properties.put ( Constants.SERVICE_DESCRIPTION, "Addon manager" );
            properties.put ( Constants.SERVICE_VENDOR, "Eclipse Package Drone" );
            this.handle = context.registerService ( AddonManager.class, this.manager, properties );
        }
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }
        if ( this.manager != null )
        {
            this.manager.dispose ();
            this.manager = null;
        }
    }

    private static Path initPath ( final BundleContext context )
    {
        try
        {
            final String dir = System.getProperty ( "package.drone.addons.dir" );
            if ( dir != null )
            {
                final Path path = Paths.get ( dir );
                Files.createDirectories ( path );
                return path;
            }
            final File dataDir = context.getDataFile ( "addons" );
            if ( dataDir != null )
            {
                Files.createDirectories ( dataDir.toPath () );
                return dataDir.toPath ();
            }

            logger.warn ( "Unable to start addon manager. No base directory available" );
            return null;
        }
        catch ( final Exception e )
        {
            logger.error ( "Failed to initialize addons directory", e );
            return null;
        }
    }
}
