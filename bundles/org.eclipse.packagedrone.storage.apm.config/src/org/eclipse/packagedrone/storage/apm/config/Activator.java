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

import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator
{

    private final static Logger logger = LoggerFactory.getLogger ( Activator.class );

    private BundleContext context;

    private ServiceRegistration<StorageManager> serviceHandle;

    private StorageManagerAdapter adapter;

    private ServiceRegistration<ManagedService> adapterHandle;

    private StorageManager service;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        this.context = context;

        final String base = System.getProperty ( "drone.storage.base" );
        if ( base != null )
        {
            registerWithPath ( base );
        }
        else
        {
            registerWithConfigAdmin ();
        }
    }

    private void registerWithConfigAdmin ()
    {
        logger.info ( "Register with config admin wrapper" );

        this.adapter = new StorageManagerAdapter ( this.context );

        final Dictionary<String, Object> properties = new Hashtable<> ();
        properties.put ( Constants.SERVICE_PID, "drone.storage.manager" );
        this.adapterHandle = this.context.registerService ( ManagedService.class, this.adapter, properties );
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        if ( this.serviceHandle != null )
        {
            logger.info ( "Unregister storage service" );
            this.serviceHandle.unregister ();
            this.serviceHandle = null;
        }
        if ( this.service != null )
        {
            logger.info ( "Stopping service" );
            this.service.close ();
            this.service = null;
        }
        if ( this.adapterHandle != null )
        {
            logger.info ( "Unregister storage service adaptor" );
            this.adapterHandle.unregister ();
            this.adapterHandle = null;
        }
        if ( this.adapter != null )
        {
            logger.info ( "Stopping storage service adaptor" );
            this.adapter.dispose ();
            this.adapter = null;
        }
    }

    private void registerWithPath ( final String base )
    {
        logger.info ( "Register with base path: {}", base );

        register ( new StorageManager ( Paths.get ( base ) ) );
    }

    private void register ( final StorageManager storageManager )
    {
        final Dictionary<String, ?> properties = new Hashtable<> ();

        this.service = storageManager;
        this.serviceHandle = this.context.registerService ( StorageManager.class, storageManager, properties );
    }

}
