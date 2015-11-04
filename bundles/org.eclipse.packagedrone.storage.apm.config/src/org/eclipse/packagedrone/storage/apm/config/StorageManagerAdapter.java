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
package org.eclipse.packagedrone.storage.apm.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageManagerAdapter implements ManagedService
{
    private final static Logger logger = LoggerFactory.getLogger ( StorageManagerAdapter.class );

    private final BundleContext context;

    private ServiceRegistration<StorageManager> handle;

    private StorageManager service;

    public StorageManagerAdapter ( final BundleContext context )
    {
        this.context = context;
    }

    @Override
    public void updated ( final Dictionary<String, ?> properties ) throws ConfigurationException
    {
        logger.warn ( "Updated - properties: {}", properties );

        try
        {
            unregister ();
            if ( properties != null )
            {
                final String base = (String)properties.get ( "basePath" );
                if ( base == null )
                {
                    throw new ConfigurationException ( "basePath", "base path is not set" );
                }
                final Path basePath = Paths.get ( base );
                register ( new StorageManager ( basePath ) );
            }
        }
        catch ( final ConfigurationException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw new ConfigurationException ( "basePath", ExceptionHelper.getMessage ( e ), e );
        }
    }

    private void register ( final StorageManager storageManager )
    {
        logger.info ( "Register service" );
        this.service = storageManager;

        final Dictionary<String, ?> properties = new Hashtable<> ();
        this.handle = this.context.registerService ( StorageManager.class, storageManager, properties );
    }

    public void dispose ()
    {
        unregister ();
    }

    private void unregister ()
    {
        if ( this.handle != null )
        {
            logger.info ( "Unregister service" );
            this.handle.unregister ();
            this.handle = null;
        }
        if ( this.service != null )
        {
            this.service.close ();
            this.service = null;
        }
    }

}
