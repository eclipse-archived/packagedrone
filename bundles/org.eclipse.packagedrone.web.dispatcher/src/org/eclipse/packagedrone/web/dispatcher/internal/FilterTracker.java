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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterTracker implements Filter
{

    private final static Logger logger = LoggerFactory.getLogger ( FilterTracker.TrackedFilter.class );

    public static class TrackedFilter
    {
        private final Filter filter;

        private final ServiceReference<Filter> reference;

        private final BundleContext context;

        public TrackedFilter ( final BundleContext context, final Filter filter, final ServiceReference<Filter> reference )
        {
            this.filter = filter;
            this.context = context;
            this.reference = reference;
        }

        public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain filterChain ) throws IOException, ServletException
        {
            this.filter.doFilter ( request, response, filterChain );
        }

        public void dispose ()
        {
            this.context.ungetService ( this.reference );
        }
    }

    private final ServiceTracker<Filter, TrackedFilter> tracker;

    private BundleContext context;

    private TrackedFilter[] filters = null;

    public FilterTracker ( final BundleContext context )
    {
        this.context = context;

        this.tracker = new ServiceTracker<> ( context, Filter.class, new ServiceTrackerCustomizer<Filter, TrackedFilter> () {

            @Override
            public TrackedFilter addingService ( final ServiceReference<Filter> reference )
            {
                return addFilter ( reference );
            }

            @Override
            public void modifiedService ( final ServiceReference<Filter> reference, final TrackedFilter service )
            {
            }

            @Override
            public void removedService ( final ServiceReference<Filter> reference, final TrackedFilter service )
            {
                service.dispose ();
                FilterTracker.this.filters = null; // reset cache
            }
        } );
    }

    protected TrackedFilter addFilter ( final ServiceReference<Filter> reference )
    {
        final Filter filter = this.context.getService ( reference );

        this.filters = null; // reset cache

        return new TrackedFilter ( this.context, filter, reference );
    }

    @Override
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain filterChain ) throws IOException, ServletException
    {
        TrackedFilter[] localFilters;
        synchronized ( this )
        {
            if ( this.filters == null )
            {
                this.filters = this.tracker.getServices ( new TrackedFilter[0] );
            }
            localFilters = this.filters;
        }

        doRunFilter ( localFilters, 0, request, response, filterChain );
    }

    private void doRunFilter ( final TrackedFilter[] localFilters, final int i, final ServletRequest request, final ServletResponse response, final FilterChain lastFilterChain ) throws IOException, ServletException
    {
        if ( i < localFilters.length )
        {
            // recurse
            localFilters[i].doFilter ( request, response, new FilterChain () {

                @Override
                public void doFilter ( final ServletRequest chainRequest, final ServletResponse chainResponse ) throws IOException, ServletException
                {
                    doRunFilter ( localFilters, i + 1, chainRequest, chainResponse, lastFilterChain );
                }
            } );
        }
        else
        {
            // abort
            lastFilterChain.doFilter ( request, response );
        }
    }

    @Override
    public void init ( final FilterConfig filterConfig ) throws ServletException
    {
        logger.info ( "Open tracker" );
        this.tracker.open ();
    }

    @Override
    public void destroy ()
    {
        logger.info ( "Close tracker" );
        this.tracker.close ();
    }

}
