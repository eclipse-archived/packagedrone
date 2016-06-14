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

import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.api.ChannelInformation;
import org.eclipse.packagedrone.repo.api.ChannelListResult;
import org.eclipse.packagedrone.repo.api.Channels;
import org.eclipse.packagedrone.repo.channel.ChannelService;

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

    private static ChannelInformation toInfo ( final org.eclipse.packagedrone.repo.channel.ChannelInformation channel )
    {
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
