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
package org.eclipse.packagedrone.utils.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.packagedrone.utils.Locks;
import org.eclipse.packagedrone.utils.Locks.Locked;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public abstract class FactoryTracker<S, T>
{
    public static class Entry<T> implements Comparable<Entry<T>>
    {
        private final String id;

        private final T service;

        private final ServiceReference<?> reference;

        public Entry ( final String id, final T service, final ServiceReference<?> reference )
        {
            this.id = id;
            this.service = service;
            this.reference = reference;
        }

        public String getId ()
        {
            return this.id;
        }

        public T getService ()
        {
            return this.service;
        }

        public ServiceReference<?> getReference ()
        {
            return this.reference;
        }

        @Override
        public int compareTo ( final Entry<T> o )
        {
            return this.reference.compareTo ( o.reference );
        }
    }

    private final ServiceTrackerCustomizer<S, Entry<T>> customizer = new ServiceTrackerCustomizer<S, Entry<T>> () {

        @Override
        public void removedService ( final ServiceReference<S> reference, final Entry<T> service )
        {
            final Entry<T> entry = removeService ( reference );
            if ( entry != null )
            {
                FactoryTracker.this.context.ungetService ( reference );
            }
        }

        @Override
        public void modifiedService ( final ServiceReference<S> reference, final Entry<T> service )
        {
        }

        @Override
        public Entry<T> addingService ( final ServiceReference<S> reference )
        {
            if ( reference == null )
            {
                return null;
            }

            final Entry<T> entry = makeEntry ( reference );

            if ( entry != null )
            {
                addEntry ( entry );
            }

            return entry;
        }
    };

    private final BundleContext context;

    private final Map<String, List<Entry<T>>> entries = new HashMap<> ();

    private final Map<ServiceReference<?>, Entry<T>> tracked = new HashMap<> ();

    private final ServiceTracker<S, Entry<T>> tracker;

    private final Lock readLock;

    private final Lock writeLock;

    /**
     * Get the factory id
     *
     * @param ref
     *            the reference to work on
     * @return the factory id for this service, or {@code null} if the service
     *         should not be considered
     */
    protected abstract String getFactoryId ( ServiceReference<S> ref );

    protected abstract T mapService ( ServiceReference<S> reference, S service );

    public FactoryTracker ( final BundleContext context, final Class<S> serviceClass )
    {
        this.context = context;
        this.tracker = new ServiceTracker<> ( context, serviceClass, this.customizer );

        final ReadWriteLock lock = new ReentrantReadWriteLock ( false );
        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();
    }

    public void open ()
    {
        try ( final Locked l = Locks.lock ( this.writeLock ) )
        {
            this.tracker.open ();
        }
    }

    public void close ()
    {
        try ( final Locked l = Locks.lock ( this.writeLock ) )
        {
            this.tracker.close ();
        }
    }

    private Entry<T> makeEntry ( final ServiceReference<S> reference )
    {
        final String id = getFactoryId ( reference );
        if ( id == null )
        {
            return null;
        }

        final S service = this.context.getService ( reference );
        if ( service == null )
        {
            return null;
        }

        return new Entry<> ( id, mapService ( reference, service ), reference );
    }

    private void addEntry ( final Entry<T> entry )
    {
        try ( Locked l = Locks.lock ( this.writeLock ) )
        {
            List<Entry<T>> list = this.entries.get ( entry.getId () );
            if ( list == null )
            {
                list = new ArrayList<> ( 1 ); // by default there would be only one
                this.entries.put ( entry.getId (), list );
            }

            this.tracked.put ( entry.getReference (), entry );

            list.add ( entry );
            Collections.sort ( list );
        }
    }

    private Entry<T> removeService ( final ServiceReference<S> ref )
    {
        try ( Locked l = Locks.lock ( this.writeLock ) )
        {
            final Entry<T> entry = this.tracked.remove ( ref );
            if ( entry == null )
            {
                return null;
            }

            final List<Entry<T>> list = this.entries.get ( entry.getId () );
            if ( list == null )
            {
                return null;
            }

            list.remove ( entry );

            if ( list.isEmpty () )
            {
                // if this was the last entry, remove it from the map
                this.entries.remove ( entry.getId () );
            }

            return entry;
        }
    }

    public void consume ( final String factoryId, final Consumer<T> consumer )
    {
        consume ( factoryId, consumer, () -> new IllegalStateException ( String.format ( "Missing factory: %s", factoryId ) ) );
    }

    public <X extends Exception> void consume ( final String factoryId, final Consumer<T> consumer, final Supplier<X> ifNotFound ) throws X
    {
        try ( Locked l = Locks.lock ( this.readLock ) )
        {
            final List<Entry<T>> entries = this.entries.get ( factoryId );

            if ( entries == null || entries.isEmpty () )
            {
                throw ifNotFound.get ();
            }

            consumer.accept ( entries.get ( 0 ).getService () );
        }
    }

    public void consumeOptionally ( final String factoryId, final Consumer<Optional<T>> consumer )
    {
        try ( Locked l = Locks.lock ( this.readLock ) )
        {
            final List<Entry<T>> entries = this.entries.get ( factoryId );

            final Optional<Entry<T>> entry = entries != null && !entries.isEmpty () ? Optional.ofNullable ( entries.get ( 0 ) ) : Optional.empty ();

            consumer.accept ( entry.map ( Entry::getService ) );
        }
    }

    public void consumeAll ( final Consumer<Stream<T>> consumer )
    {
        consumeEntries ( stream -> consumer.accept ( stream.map ( Entry::getService ) ) );
    }

    public void consumeEntries ( final Consumer<Stream<Entry<T>>> consumer )
    {
        try ( final Locked l = Locks.lock ( this.readLock ) )
        {
            try ( final Stream<Entry<T>> stream = this.entries.values ().stream ().flatMap ( list -> {
                final Optional<Entry<T>> first = list.stream ().findFirst ();
                return first.map ( e -> Stream.of ( e ) ).orElseGet ( Stream::empty );
            } ) )
            {
                consumer.accept ( stream );
            }
        }
    }

    public static String getString ( final ServiceReference<?> ref, final String key )
    {
        final Object value = ref.getProperty ( key );
        if ( value instanceof String )
        {
            return (String)value;
        }
        return null;
    }
}
