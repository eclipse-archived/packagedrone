/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.impl;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider;

public class ChannelInstance implements ProviderListener
{
    private Channel channel;

    private @NonNull final String channelId;

    private @NonNull final String providerId;

    private @NonNull final Map<MetaKey, String> configuration;

    private @NonNull final ChannelProviderTracker providerTracker;

    public ChannelInstance ( @NonNull final String channelId, @NonNull final String providerId, @NonNull final Map<MetaKey, String> configuration, @NonNull final ChannelProviderTracker providerTracker )
    {
        this.channelId = channelId;
        this.providerId = providerId;
        this.configuration = configuration;

        this.providerTracker = providerTracker;
        providerTracker.addListener ( providerId, this );
    }

    public @NonNull String getChannelId ()
    {
        return this.channelId;
    }

    public @NonNull String getProviderId ()
    {
        return this.providerId;
    }

    public @NonNull Map<MetaKey, String> getConfiguration ()
    {
        return this.configuration;
    }

    public @NonNull Optional<Channel> getChannel ()
    {
        return Optional.ofNullable ( this.channel );
    }

    @Override
    public void bind ( @NonNull final ChannelProvider provider )
    {
        this.channel = provider.load ( this.channelId );
    }

    @Override
    public void unbind ()
    {
        if ( this.channel != null )
        {
            this.channel.dispose ();
            this.channel = null;
        }
    }

    public void dispose ()
    {
        this.providerTracker.removeListener ( this.providerId, this );
        unbind ();
    }
}
