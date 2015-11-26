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
package org.eclipse.packagedrone.repo.web.utils;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;

import java.util.Optional;

import org.eclipse.packagedrone.repo.channel.ChannelArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.common.CommonController;

public final class Channels
{
    private Channels ()
    {
    }

    @FunctionalInterface
    public interface ArtifactOperation<T extends ReadableChannel>
    {
        public ModelAndView process ( T target, ChannelArtifactInformation artifact ) throws Exception;
    }

    public static <T> ModelAndView withChannel ( final ChannelService service, final String channelId, final Class<T> clazz, final ChannelOperation<ModelAndView, T> operation )
    {
        try
        {
            return service.accessCall ( By.id ( channelId ), clazz, operation );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    public static <T extends ReadableChannel> ModelAndView withArtifact ( final ChannelService service, final String channelId, final String artifactId, final Class<T> clazz, final ArtifactOperation<T> operation )
    {
        return Channels.withChannel ( service, channelId, clazz, channel -> {

            final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( artifactId );
            if ( !artifact.isPresent () )
            {
                return CommonController.createNotFound ( "artifact", artifactId );
            }

            return operation.process ( channel, artifact.get () );
        } );
    }

    public static ModelAndView redirectViewArtifact ( final ChannelArtifactInformation artifact )
    {
        return redirectViewArtifact ( artifact.getChannelId ().getId (), artifact.getId () );
    }

    public static ModelAndView redirectViewArtifact ( final String channelId, final String artifactId )
    {
        return new ModelAndView ( "redirect:/channel/" + urlPathSegmentEscaper ().escape ( channelId ) + "/artifacts/" + urlPathSegmentEscaper ().escape ( artifactId ) + "/view" );
    }

    public static ModelAndView redirectViewChannel ( final ChannelId channel )
    {
        return redirectViewChannel ( channel.getId () );
    }

    public static ModelAndView redirectViewChannel ( final String channelId )
    {
        return new ModelAndView ( "redirect:" + channelTarget ( channelId ) );
    }

    public static String channelTarget ( final String channelId )
    {
        return "/channel/" + urlPathSegmentEscaper ().escape ( channelId ) + "/view";
    }
}
