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
package org.eclipse.packagedrone.repo.web.utils;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.web.ModelAndView;

public abstract class ChannelServiceController
{
    private ChannelService channelService;

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    protected <T> ModelAndView withChannel ( final String channelId, final Class<T> clazz, final ChannelOperation<ModelAndView, T> operation )
    {
        if ( this.channelService == null )
        {
            throw new IllegalStateException ( "ChannelService not set for controller. ChannelService has to be bound to 'setChannelService' in OSGi DS declaration." );
        }

        return Channels.withChannel ( this.channelService, channelId, clazz, operation );
    }
}
