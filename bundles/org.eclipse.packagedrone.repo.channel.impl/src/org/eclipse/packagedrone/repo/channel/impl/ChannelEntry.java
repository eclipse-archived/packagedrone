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
package org.eclipse.packagedrone.repo.channel.impl;

import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider;

class ChannelEntry
{
    private ChannelId id;

    private final Channel channel;

    private final ChannelProvider provider;

    public ChannelEntry ( final ChannelId id, final Channel channel, final ChannelProvider provider )
    {
        this.id = id;
        this.channel = channel;
        this.provider = provider;
    }

    public ChannelId getId ()
    {
        return this.id;
    }

    public void setId ( final ChannelId id )
    {
        this.id = id;
    }

    public Channel getChannel ()
    {
        return this.channel;
    }

    public ChannelProvider getProvider ()
    {
        return this.provider;
    }
}
