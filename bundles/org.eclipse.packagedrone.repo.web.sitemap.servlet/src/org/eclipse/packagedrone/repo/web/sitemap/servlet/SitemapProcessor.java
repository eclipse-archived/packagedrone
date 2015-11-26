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

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;

import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContextCreator;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapGenerator;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapIndexWriter;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContext;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetWriter;
import org.eclipse.packagedrone.utils.Exceptions.ThrowingRunnable;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class SitemapProcessor
{
    private final Supplier<String> prefixSupplier;

    private final String sitemapUrl;

    private final XMLOutputFactory outputFactory;

    private final ServiceTracker<SitemapGenerator, SitemapGenerator> tracker;

    public SitemapProcessor ( final Supplier<String> prefixSupplier, final String sitemapUrl, final XMLOutputFactory outputFactory )
    {
        this.prefixSupplier = prefixSupplier;
        this.sitemapUrl = sitemapUrl;
        this.outputFactory = outputFactory;

        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( SitemapProcessor.class ).getBundleContext (), SitemapGenerator.class, null );
        this.tracker.open ();
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    public boolean process ( final HttpServletResponse response, final String path ) throws IOException
    {
        if ( path == null || path.isEmpty () )
        {
            processRoot ( response );
            return true;
        }
        else
        {
            return processSub ( response, path );
        }
    }

    private void processRoot ( final HttpServletResponse response ) throws IOException
    {
        final String prefix = ofNullable ( this.prefixSupplier.get () ).orElse ( "http://localhost" ) + this.sitemapUrl;

        response.setContentType ( "text/xml" );

        try ( SitemapIndexWriter writer = new SitemapIndexWriter ( response.getWriter (), prefix, this.outputFactory ) )
        {
            for ( final SitemapGenerator generator : this.tracker.getTracked ().values () )
            {
                generator.gatherRoots ( writer );
            }
        }
    }

    private class ContextCreator implements UrlSetContextCreator
    {
        private final HttpServletResponse response;

        private boolean used;

        private ThrowingRunnable finish;

        public ContextCreator ( final HttpServletResponse response )
        {
            this.response = response;
        }

        public boolean isUsed ()
        {
            return this.used;
        }

        @Override
        public UrlSetContext createUrlSet ()
        {
            if ( this.used )
            {
                throw new IllegalStateException ( "Context creator may only be used once" );
            }

            this.used = true;

            try
            {
                final String prefix = ofNullable ( SitemapProcessor.this.prefixSupplier.get () ).orElse ( "http://localhost" );
                this.response.setContentType ( "text/xml" );

                final UrlSetWriter result = new UrlSetWriter ( this.response.getWriter (), prefix, SitemapProcessor.this.outputFactory );

                this.finish = result::finish;

                return result;
            }
            catch ( final IOException e )
            {
                throw new RuntimeException ( e );
            }
        }

        public void finish ()
        {
            if ( this.finish != null )
            {
                try
                {
                    this.finish.run ();
                }
                catch ( final Exception e )
                {
                    // ignore
                }
            }
        }

    }

    private boolean processSub ( final HttpServletResponse response, final String path ) throws IOException
    {
        final ContextCreator creator = new ContextCreator ( response );

        for ( final SitemapGenerator generator : this.tracker.getTracked ().values () )
        {
            generator.render ( path, creator );
            if ( creator.isUsed () )
            {
                creator.finish ();
                return true;
            }
        }

        return false;
    }

}
