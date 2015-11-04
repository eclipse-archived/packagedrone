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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.Severity;
import org.eclipse.packagedrone.repo.aspect.aggregate.AggregationContext;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.repo.channel.ChannelService.ArtifactReceiver;
import org.eclipse.packagedrone.repo.utils.IOConsumer;

public class AggregationContextImpl implements AggregationContext
{
    private final AspectableContext ctx;

    private final String aspectId;

    private final String channelId;

    private final Supplier<ChannelDetails> details;

    private final Consumer<ValidationMessage> msgHandler;

    public AggregationContextImpl ( final AspectableContext ctx, final String aspectId, final String channelId, final Supplier<ChannelDetails> details, final Consumer<ValidationMessage> msgHandler )
    {
        this.ctx = ctx;
        this.aspectId = aspectId;
        this.channelId = channelId;

        this.details = details;

        this.msgHandler = msgHandler;
    }

    @Override
    public void validationMessage ( final Severity severity, final String message, final Set<String> artifactIds )
    {
        this.msgHandler.accept ( new ValidationMessage ( this.aspectId, severity, message, artifactIds ) );
    }

    @Override
    public Collection<ArtifactInformation> getArtifacts ()
    {
        return Collections.unmodifiableCollection ( this.ctx.getArtifacts ().values () );
    }

    @Override
    public String getChannelId ()
    {
        return this.channelId;
    }

    @Override
    public String getChannelDescription ()
    {
        return this.details.get ().getDescription ();
    }

    @Override
    public Map<MetaKey, String> getChannelMetaData ()
    {
        return Collections.unmodifiableMap ( this.ctx.getChannelProvidedMetaData () );
    }

    @Override
    public void createCacheEntry ( final String id, final String name, final String mimeType, final IOConsumer<OutputStream> creator ) throws IOException
    {
        this.ctx.createCacheEntry ( new MetaKey ( this.aspectId, id ), name, mimeType, creator );
    }

    @Override
    public boolean streamArtifact ( final String artifactId, final ArtifactReceiver receiver ) throws IOException
    {
        final ArtifactInformation artifact = this.ctx.getArtifacts ().get ( artifactId );
        if ( artifact == null )
        {
            return false;
        }

        return this.ctx.stream ( artifactId, stream -> receiver.consume ( artifact, stream ) );
    }

    @Override
    public boolean streamArtifact ( final String artifactId, final IOConsumer<InputStream> consumer ) throws IOException
    {
        return this.ctx.stream ( artifactId, consumer );
    }
}
