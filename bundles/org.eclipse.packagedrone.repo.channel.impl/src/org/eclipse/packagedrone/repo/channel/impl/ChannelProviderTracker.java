/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.impl;

import static org.eclipse.packagedrone.utils.Locks.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider;
import org.eclipse.packagedrone.utils.Locks.Locked;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ChannelProviderTracker
{
    private Lock readLock;

    private Lock writeLock;

    private ServiceTracker<ChannelProvider, ChannelProvider> tracker;

    private final Map<String, ChannelProvider> providers = new HashMap<> ();

    private final Multimap<String, ProviderListener> listeners = HashMultimap.create ();

    public ChannelProviderTracker ( final BundleContext context )
    {
        final ReadWriteLock lock = new ReentrantReadWriteLock ( false );

        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();

        this.tracker = new ServiceTracker<> ( context, ChannelProvider.class, new ServiceTrackerCustomizer<ChannelProvider, ChannelProvider> () {

            @Override
            public ChannelProvider addingService ( final ServiceReference<ChannelProvider> reference )
            {
                final ChannelProvider service = context.getService ( reference );
                addService ( service );
                return service;
            }

            @Override
            public void modifiedService ( final ServiceReference<ChannelProvider> reference, final ChannelProvider service )
            {
            }

            @Override
            public void removedService ( final ServiceReference<ChannelProvider> reference, final ChannelProvider service )
            {
                try
                {
                    removeService ( service );
                }
                finally
                {
                    context.ungetService ( reference );
                }
            }
        } );
    }

    protected void addService ( final ChannelProvider service )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.providers.put ( service.getId (), service );
            for ( final ProviderListener listener : this.listeners.get ( service.getId () ) )
            {
                listener.bind ( service );
            }
        }
    }

    protected void removeService ( final ChannelProvider service )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.providers.remove ( service.getId () );

            for ( final ProviderListener listener : this.listeners.get ( service.getId () ) )
            {
                listener.unbind ();
            }
        }
    }

    public void addListener ( final String providerId, final ProviderListener listener )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.listeners.put ( providerId, listener );

            final ChannelProvider provider = this.providers.get ( providerId );
            if ( provider != null )
            {
                listener.bind ( provider );
            }
        }
    }

    public void removeListener ( final String providerId, final ProviderListener listener )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.listeners.remove ( providerId, listener );
        }
    }

    public void run ( final String id, final Consumer<Optional<ChannelProvider>> run )
    {
        Objects.requireNonNull ( run );

        try ( Locked l = lock ( this.readLock ) )
        {
            run.accept ( Optional.ofNullable ( this.providers.get ( id ) ) );
        }
    }

    public <R> R call ( final String id, final Function<Optional<ChannelProvider>, R> call )
    {
        Objects.requireNonNull ( call );

        try ( Locked l = lock ( this.readLock ) )
        {
            return call.apply ( Optional.ofNullable ( this.providers.get ( id ) ) );
        }
    }

    public void start ()
    {
        this.tracker.open ();
    }

    public void stop ()
    {
        this.tracker.close ();
    }
}
