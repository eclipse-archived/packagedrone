/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagDirTracker implements ResourceProvider
{
    private final static Logger logger = LoggerFactory.getLogger ( TagDirTracker.class );

    private final BundleTracker<TagDirInfo> bundleTracker;

    private final BundleTrackerCustomizer<TagDirInfo> customizer = new BundleTrackerCustomizer<TagDirInfo> () {

        @Override
        public TagDirInfo addingBundle ( final Bundle bundle, final BundleEvent event )
        {
            return createTagLibInfo ( bundle );
        }

        @Override
        public void modifiedBundle ( final Bundle bundle, final BundleEvent event, final TagDirInfo object )
        {
        }

        @Override
        public void removedBundle ( final Bundle bundle, final BundleEvent event, final TagDirInfo object )
        {
        }
    };

    private static class TagDirInfo
    {
        private final String prefix;

        private final Bundle bundle;

        public TagDirInfo ( final Bundle bundle, final String prefix )
        {
            this.bundle = bundle;
            this.prefix = prefix;
        }

        public Bundle getBundle ()
        {
            return this.bundle;
        }

        public String getPrefix ()
        {
            return this.prefix;
        }

        public URL getUrl ()
        {
            return this.bundle.getEntry ( this.prefix );
        }

        public List<String> getDirectoryUrls ()
        {
            final List<String> result = new LinkedList<> ();
            final Enumeration<String> en = this.bundle.getEntryPaths ( this.prefix );
            while ( en.hasMoreElements () )
            {
                result.add ( "/" + en.nextElement () );
            }
            return result;
        }

        @Override
        public String toString ()
        {
            return String.format ( "[TagDir - prefix: %s, bundle: %s]", this.prefix, this.bundle );
        }
    }

    public TagDirTracker ( final BundleContext context )
    {
        this.bundleTracker = new BundleTracker<> ( context, Bundle.RESOLVED | Bundle.ACTIVE, this.customizer );
        this.bundleTracker.open ();
    }

    protected TagDirInfo createTagLibInfo ( final Bundle bundle )
    {
        logger.trace ( "Checking for tag dir directories: {}", bundle );

        final String tld = bundle.getHeaders ().get ( "Web-TagLib-Directory" );
        if ( tld != null )
        {
            final TagDirInfo result = new TagDirInfo ( bundle, tld );
            logger.debug ( "Found tag directory: {}", result );
            return result;
        }
        return null;
    }

    @Override
    public void dispose ()
    {
        this.bundleTracker.close ();
    }

    @Override
    public URL getResource ( final String name )
    {
        logger.trace ( "Getting tag dir for: {}", name );

        for ( final TagDirInfo tli : this.bundleTracker.getTracked ().values () )
        {
            if ( name.startsWith ( tli.getPrefix () ) )
            {
                logger.trace ( "Trying {} for {}", tli, name );
                final URL result = tli.getBundle ().getEntry ( name );
                if ( result != null )
                {
                    logger.trace ( "Using {} for {}", tli, name );
                    return result;
                }
            }
        }

        return null;
    }

    @Override
    public Set<String> getPaths ( final String name )
    {
        logger.trace ( "Getting url for: {}", name );

        final Set<String> result = new HashSet<> ();

        for ( final TagDirInfo tli : this.bundleTracker.getTracked ().values () )
        {
            if ( tli.getPrefix ().equals ( name ) )
            {
                final URL url = tli.getUrl ();
                if ( url != null )
                {
                    result.addAll ( tli.getDirectoryUrls () );
                }
            }
        }

        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Result: {}", result );
        }

        return result;
    }
}
