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

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.DescriptorAdapter;

class DescriptorAdapterImpl implements DescriptorAdapter
{
    private final ChannelServiceModify channel;

    private final String channelId;

    public DescriptorAdapterImpl ( final String channelId, final ChannelServiceModify channel )
    {
        this.channelId = channelId;
        this.channel = channel;
    }

    @Override
    public void addName ( final String name )
    {
        this.channel.putMapping ( this.channelId, name );
    }

    @Override
    public void removeName ( final String name )
    {
        this.channel.deleteMapping ( this.channelId, name );
    }

    @Override
    public void setNames ( final Collection<String> names )
    {
        this.channel.setNameMappings ( this.channelId, names );
    }

    @Override
    public void setDescription ( final String description )
    {
        this.channel.setDescription ( this.channelId, description );
    }

    @Override
    public ChannelId getDescriptor ()
    {
        return new ChannelId ( this.channelId, new LinkedHashSet<> ( this.channel.getNameMappings ( this.channelId ) ), this.channel.getDescription ( this.channelId ) );
    }
}
