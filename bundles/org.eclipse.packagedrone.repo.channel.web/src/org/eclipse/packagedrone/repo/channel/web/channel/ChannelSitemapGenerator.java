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
package org.eclipse.packagedrone.repo.channel.web.channel;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.Optional;

import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.web.sitemap.ChangeFrequency;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapGenerator;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapIndexContext;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContext;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContextCreator;

public class ChannelSitemapGenerator implements SitemapGenerator
{
    private ChannelService channelService;

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    @Override
    public void gatherRoots ( final SitemapIndexContext context )
    {
        context.addLocation ( "channels", calcLastMod () );
    }

    @Override
    public void render ( final String path, final UrlSetContextCreator creator )
    {
        final String[] toks = path.split ( "/" );

        if ( toks.length == 1 && "channels".equals ( toks[0] ) )
        {
            // write out url set of all channels

            final UrlSetContext context = creator.createUrlSet ();

            for ( final ChannelInformation ci : this.channelService.list () )
            {
                final Optional<Instant> lastMod = ofNullable ( ci.getState ().getModificationTimestamp () );
                final String id = urlPathSegmentEscaper ().escape ( ci.getId () );

                context.addLocation ( String.format ( "/channel/%s/view", id ), lastMod, of ( ChangeFrequency.DAILY ), empty () );
                context.addLocation ( String.format ( "/channel/%s/viewPlain", id ), lastMod, of ( ChangeFrequency.DAILY ), empty () );
                context.addLocation ( String.format ( "/channel/%s/details", id ), lastMod, of ( ChangeFrequency.DAILY ), empty () );
                context.addLocation ( String.format ( "/channel/%s/validation", id ), lastMod, of ( ChangeFrequency.DAILY ), empty () );
            }
        }
    }

    /**
     * Find the last modification timestamp of all channels
     *
     * @return the latest modification timestamp of all channels
     */
    private Optional<Instant> calcLastMod ()
    {
        Instant globalLastMod = null;

        for ( final ChannelInformation ci : this.channelService.list () )
        {
            final Optional<Instant> lastMod = ofNullable ( ci.getState ().getModificationTimestamp () );

            if ( globalLastMod == null || lastMod.get ().isAfter ( globalLastMod ) )
            {
                globalLastMod = lastMod.get ();
            }
        }
        return Optional.ofNullable ( globalLastMod );
    }

}
