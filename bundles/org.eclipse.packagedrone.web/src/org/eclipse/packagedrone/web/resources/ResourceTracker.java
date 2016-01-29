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
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.utils.AttributedValue;
import org.eclipse.packagedrone.utils.Headers;
import org.eclipse.packagedrone.web.BundleResourceNotFoundRequestHandler;
import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.RequestHandlerFactory;
import org.eclipse.packagedrone.web.ResourceRequestHandler;
import org.eclipse.packagedrone.web.util.Requests;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceTracker implements RequestHandlerFactory
{
    private final static Logger logger = LoggerFactory.getLogger ( ResourceTracker.class );

    private final org.osgi.util.tracker.BundleTracker<ResourceHandlerProvider> tracker;

    private static class CompositeResourceEntry implements ResourceHandlerProvider
    {

        private final List<ResourceHandlerProvider> providers;

        public CompositeResourceEntry ( final List<ResourceHandlerProvider> providers )
        {
            this.providers = providers;
        }

        @Override
        public RequestHandler findHandler ( final String requestPath )
        {
            for ( final ResourceHandlerProvider provider : this.providers )
            {
                final RequestHandler resource = provider.findHandler ( requestPath );
                if ( resource != null )
                {
                    return resource;
                }
            }
            return null;
        }

    }

    private static class ResourceEntry implements ResourceHandlerProvider
    {
        private final Bundle bundle;

        private final String prefix;

        private final String target;

        public ResourceEntry ( final Bundle bundle, final String prefix, final String target )
        {
            this.bundle = bundle;
            this.prefix = prefix;
            this.target = target;
        }

        @Override
        public RequestHandler findHandler ( final String requestPath )
        {
            if ( !requestPath.startsWith ( this.target ) )
            {
                return null;
            }

            final String entryName = this.prefix + requestPath.substring ( this.target.length () );
            final URL entry = this.bundle.getEntry ( entryName );
            if ( entry == null )
            {
                logger.info ( "Resource '{}' could not be found in bundle {} ({})", requestPath, this.bundle.getBundleId (), this.bundle.getSymbolicName () );
                return new BundleResourceNotFoundRequestHandler ( this.bundle, entryName );
            }
            else
            {
                return new ResourceRequestHandler ( entry, this.bundle.getLastModified () );
            }
        }
    }

    private final BundleTrackerCustomizer<ResourceHandlerProvider> customizer = new BundleTrackerCustomizer<ResourceHandlerProvider> () {

        @Override
        public void removedBundle ( final Bundle bundle, final BundleEvent event, final ResourceHandlerProvider object )
        {
        }

        @Override
        public void modifiedBundle ( final Bundle bundle, final BundleEvent event, final ResourceHandlerProvider object )
        {
        }

        @Override
        public ResourceHandlerProvider addingBundle ( final Bundle bundle, final BundleEvent event )
        {
            final List<ResourceHandlerProvider> entries = new LinkedList<> ();

            {
                final String resources = bundle.getHeaders ().get ( "Web-Static-Resources" );
                final List<AttributedValue> result = Headers.parseList ( resources );
                if ( result != null )
                {
                    for ( final AttributedValue av : result )
                    {
                        final String prefix = av.getValue ();
                        String target = av.getAttributes ().get ( "target" );
                        if ( target == null )
                        {
                            target = prefix;
                        }
                        entries.add ( new ResourceEntry ( bundle, prefix, target ) );
                    }
                }
            }

            if ( entries.isEmpty () )
            {
                return null;
            }
            else if ( entries.size () == 1 )
            {
                return entries.get ( 0 );
            }
            else
            {
                return new CompositeResourceEntry ( entries );
            }
        }

    };

    public ResourceTracker ( final BundleContext context )
    {
        this.tracker = new BundleTracker<> ( context, Bundle.ACTIVE | Bundle.INSTALLED, this.customizer );
        this.tracker.open ();
    }

    @Override
    public void close ()
    {
        this.tracker.close ();
    }

    @Override
    public RequestHandler handleRequest ( final HttpServletRequest request, final HttpServletResponse response )
    {
        final String method = request.getMethod ().toUpperCase ();
        if ( ! ( method.equals ( "GET" ) || method.equals ( "HEAD" ) ) )
        {
            // static resources only support GET or HEAD
            return null;
        }

        final String requestPath = Requests.getRequestPath ( request );

        for ( final ResourceHandlerProvider entry : this.tracker.getTracked ().values () )
        {
            final RequestHandler handler = entry.findHandler ( requestPath );
            if ( handler != null )
            {
                return handler;
            }
        }
        return null;
    }

}
