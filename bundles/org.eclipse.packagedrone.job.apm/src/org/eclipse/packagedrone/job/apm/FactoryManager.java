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
package org.eclipse.packagedrone.job.apm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.packagedrone.job.JobFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class FactoryManager
{
    private final BundleContext context;

    private final Map<String, JobFactory> services = new HashMap<> ();

    private final Map<ServiceReference<JobFactory>, String> refMap = new HashMap<> ();

    private final ReadLock readLock;

    private final WriteLock writeLock;

    private final ServiceTracker<JobFactory, JobFactory> tracker;

    private final ServiceTrackerCustomizer<JobFactory, JobFactory> customizer = new ServiceTrackerCustomizer<JobFactory, JobFactory> () {

        @Override
        public JobFactory addingService ( final ServiceReference<JobFactory> reference )
        {
            return handleAdding ( reference );
        }

        @Override
        public void modifiedService ( final ServiceReference<JobFactory> reference, final JobFactory service )
        {
            handleModified ( reference, service );
        }

        @Override
        public void removedService ( final ServiceReference<JobFactory> reference, final JobFactory service )
        {
            handleRemoved ( reference, service );
        }

    };

    private static class Locked implements AutoCloseable
    {
        private final Lock lock;

        public Locked ( final Lock lock )
        {
            lock.lock ();
            this.lock = lock;
        }

        @Override
        public void close ()
        {
            this.lock.unlock ();
        }
    }

    public FactoryManager ( final BundleContext context )
    {
        this.context = context;

        final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock ();

        this.readLock = rwLock.readLock ();
        this.writeLock = rwLock.writeLock ();

        this.tracker = new ServiceTracker<> ( context, JobFactory.class, this.customizer );
    }

    public void start ()
    {
        try ( Locked l = new Locked ( this.writeLock ) )
        {
            this.tracker.open ();
        }
    }

    public void stop ()
    {
        try ( Locked l = new Locked ( this.writeLock ) )
        {
            this.tracker.close ();
        }
    }

    private static String getId ( final ServiceReference<JobFactory> reference )
    {
        final Object idValue = reference.getProperty ( JobFactory.FACTORY_ID );

        if ( idValue instanceof String )
        {
            return (String)idValue;
        }
        else if ( idValue instanceof String[] )
        {
            return ( (String[])idValue )[0];
        }
        else
        {
            return null;
        }
    }

    protected JobFactory handleAdding ( final ServiceReference<JobFactory> reference )
    {
        try ( Locked l = new Locked ( this.writeLock ) )
        {
            final String id = getId ( reference );
            if ( id == null )
            {
                return null;
            }

            final JobFactory service = this.context.getService ( reference );

            putService ( id, reference, service );

            return service;
        }
    }

    private void putService ( final String id, final ServiceReference<JobFactory> reference, final JobFactory service )
    {
        this.services.put ( id, service );
        this.refMap.put ( reference, id );
    }

    protected void handleModified ( final ServiceReference<JobFactory> reference, final JobFactory service )
    {
        try ( Locked l = new Locked ( this.writeLock ) )
        {
            removeService ( reference );

            final String newId = getId ( reference );
            if ( newId == null )
            {
                return; // service is still being tracked
            }
            putService ( newId, reference, service );
        }
    }

    private void removeService ( final ServiceReference<JobFactory> reference )
    {
        final String oldId = this.refMap.remove ( reference );
        if ( oldId != null )
        {
            this.services.remove ( oldId );
        }
    }

    protected synchronized void handleRemoved ( final ServiceReference<JobFactory> reference, final JobFactory service )
    {
        try ( Locked l = new Locked ( this.writeLock ) )
        {
            removeService ( reference );
        }
    }

    public Optional<JobFactory> getFactory ( final String id )
    {
        try ( Locked l = new Locked ( this.readLock ) )
        {
            return Optional.ofNullable ( this.services.get ( id ) );
        }
    }
}
