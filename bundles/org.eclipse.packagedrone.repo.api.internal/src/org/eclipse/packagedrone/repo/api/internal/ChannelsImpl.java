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
package org.eclipse.packagedrone.repo.api.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.api.ChannelInformation;
import org.eclipse.packagedrone.repo.api.ChannelListResult;
import org.eclipse.packagedrone.repo.api.Channels;
import org.eclipse.packagedrone.repo.api.CreateChannel;
import org.eclipse.packagedrone.repo.channel.AspectableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.DescriptorAdapter;

public class ChannelsImpl implements Channels
{
    private ChannelService channelService;

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    @Override
    public ChannelListResult list ()
    {
        final ChannelListResult result = new ChannelListResult ();
        result.setChannels ( this.channelService.list ().stream ().map ( ChannelsImpl::toInfo ).collect ( Collectors.toList () ) );
        return result;
    }

    @Override
    public ChannelInformation createChannel ( final CreateChannel createChannel )
    {
        Objects.requireNonNull ( createChannel, "Missing channel information" );

        final ChannelDetails details = new ChannelDetails ();
        details.setDescription ( createChannel.getDescription () );

        final Map<MetaKey, String> configuration = new HashMap<> ();

        final ChannelId id = this.channelService.create ( null, details, configuration );
        if ( id == null )
        {
            return null;
        }

        try
        {
            if ( createChannel.getNames () != null && !createChannel.getNames ().isEmpty () )
            {
                this.channelService.accessRun ( By.id ( id.getId () ), DescriptorAdapter.class, channel -> {
                    channel.setNames ( createChannel.getNames () );
                } );
            }

            if ( createChannel.getAspects () != null && !createChannel.getAspects ().isEmpty () )
            {
                this.channelService.accessRun ( By.id ( id.getId () ), AspectableChannel.class, channel -> {
                    channel.addAspects ( createChannel.isAspectsWithDependencies (), createChannel.getAspects () );
                } );
            }
        }
        catch ( final Exception e )
        {
            this.channelService.delete ( By.id ( id.getId () ) );
            throw e;
        }

        return toInfo ( this.channelService.getState ( By.id ( id.getId () ) ).orElse ( null ) );
    }

    private static ChannelInformation toInfo ( final org.eclipse.packagedrone.repo.channel.ChannelInformation channel )
    {
        if ( channel == null )
        {
            return null;
        }

        final ChannelInformation result = new ChannelInformation ();

        result.setId ( channel.getId () );
        if ( channel.getDescription () != null && !channel.getDescription ().isEmpty () )
        {
            result.setDescription ( channel.getDescription () );
        }

        if ( channel.getShortDescription () != null && !channel.getShortDescription ().isEmpty () )
        {
            result.setShortDescription ( channel.getShortDescription () );
        }

        result.setNames ( channel.getNames () );
        result.setAspects ( channel.getAspectStates ().keySet () );

        return result;
    }
}
