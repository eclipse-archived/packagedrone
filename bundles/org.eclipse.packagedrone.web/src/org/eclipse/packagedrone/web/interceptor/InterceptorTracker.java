/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH and other.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.interceptor;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class InterceptorTracker implements InterceptorLocator
{
    private final ServiceTracker<Interceptor, Interceptor> tracker;

    private final ServiceTrackerCustomizer<Interceptor, Interceptor> customizer = new ServiceTrackerCustomizer<Interceptor, Interceptor> () {

        @Override
        public void removedService ( final ServiceReference<Interceptor> reference, final Interceptor service )
        {
            removeInterceptor ( reference );
            InterceptorTracker.this.context.ungetService ( reference );
        }

        @Override
        public void modifiedService ( final ServiceReference<Interceptor> reference, final Interceptor service )
        {
            removeInterceptor ( reference );
            addInterceptor ( service, reference );
        }

        @Override
        public Interceptor addingService ( final ServiceReference<Interceptor> reference )
        {
            final Interceptor service = InterceptorTracker.this.context.getService ( reference );
            addInterceptor ( service, reference );
            return service;
        }
    };

    private final BundleContext context;

    private final SortedMap<ServiceReference<?>, Interceptor> cacheMap = new TreeMap<> ( new Comparator<ServiceReference<?>> () {

        @Override
        public int compare ( final ServiceReference<?> o1, final ServiceReference<?> o2 )
        {
            return o2.compareTo ( o1 );
        }
    } );

    private volatile Interceptor[] cache = new Interceptor[0];

    public InterceptorTracker ( final BundleContext context )
    {
        this.context = context;
        this.tracker = new ServiceTracker<> ( context, Interceptor.class, this.customizer );
        this.tracker.open ();
    }

    protected synchronized void addInterceptor ( final Interceptor service, final ServiceReference<Interceptor> reference )
    {
        this.cacheMap.put ( reference, service );
        updateCache ();
    }

    protected synchronized void removeInterceptor ( final ServiceReference<Interceptor> reference )
    {
        this.cacheMap.remove ( reference );
        updateCache ();
    }

    private void updateCache ()
    {
        this.cache = this.cacheMap.values ().toArray ( new Interceptor[this.cacheMap.size ()] );
    }

    @Override
    public void close ()
    {
        this.tracker.close ();
        this.cache = new Interceptor[0]; // just in case
    }

    @Override
    public Interceptor[] getInterceptors ()
    {
        return this.cache;
    }
}
