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
package org.eclipse.packagedrone.repo.web.sitemap.servlet;

import java.time.Instant;
import java.util.Optional;

import org.eclipse.packagedrone.repo.web.sitemap.ChangeFrequency;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapExtender;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapGenerator;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapIndexContext;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContext;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContextCreator;
import org.eclipse.scada.utils.lang.Holder;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class MainSitemapGenerator implements SitemapGenerator
{
    private final ServiceTracker<SitemapExtender, SitemapExtender> tracker;

    public MainSitemapGenerator ()
    {
        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( MainSitemapGenerator.class ).getBundleContext (), SitemapExtender.class, null );
        this.tracker.open ();
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    @Override
    public void gatherRoots ( final SitemapIndexContext context )
    {
        context.addLocation ( "main", findLastMod () );
    }

    @Override
    public void render ( final String path, final UrlSetContextCreator contextCreator )
    {
        if ( !path.equals ( "main" ) )
        {
            return;
        }

        final UrlSetContext context = contextCreator.createUrlSet ();

        for ( final SitemapExtender extender : this.tracker.getTracked ().values () )
        {
            extender.extend ( context );
        }
    }

    protected Optional<Instant> findLastMod ()
    {
        final Holder<Instant> lastMod = new Holder<> ();

        for ( final SitemapExtender extender : this.tracker.getTracked ().values () )
        {
            extender.extend ( new UrlSetContext () {

                @Override
                public void addLocation ( final String localUrl, final Optional<Instant> lastModification, final Optional<ChangeFrequency> changeFrequency, final Optional<Double> priority )
                {
                    if ( !lastModification.isPresent () )
                    {
                        return;
                    }

                    if ( lastMod.value == null )
                    {
                        lastMod.value = lastModification.get ();
                    }
                    else if ( lastModification.get ().isAfter ( lastMod.value ) )
                    {
                        lastMod.value = lastModification.get ();
                    }
                }
            } );
        }

        return Optional.ofNullable ( lastMod.value );
    }

}
