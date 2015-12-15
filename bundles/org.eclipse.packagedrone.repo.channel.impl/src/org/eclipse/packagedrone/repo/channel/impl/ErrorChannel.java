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

import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.Severity;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntry;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.channel.ChannelState.Builder;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.repo.channel.provider.AccessContext;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;
import org.eclipse.packagedrone.utils.Exceptions;
import org.eclipse.packagedrone.utils.io.IOConsumer;

/**
 * A readonly channel in error state
 */
final class ErrorChannel implements Channel
{
    private final String errorMessage;

    public ErrorChannel ( final String errorMessage )
    {
        this.errorMessage = errorMessage;
    }

    @Override
    public <T> T modifyCall ( final ChannelOperation<T, ModifyContext> operation )
    {
        throw new UnsupportedOperationException ();
    }

    @Override
    public void dispose ()
    {
    }

    @Override
    public void delete ()
    {
        throw new UnsupportedOperationException ();
    }

    @Override
    public <T> T accessCall ( final ChannelOperation<T, AccessContext> operation )
    {
        return Exceptions.wrapException ( () -> operation.process ( new AccessContext () {

            @Override
            public boolean streamCacheEntry ( final MetaKey key, final IOConsumer<CacheEntry> consumer )
            {
                return false;
            }

            @Override
            public boolean stream ( final String artifactId, final IOConsumer<InputStream> consumer )
            {
                return false;
            }

            @Override
            public ChannelState getState ()
            {
                final Builder builder = new ChannelState.Builder ();
                final Instant now = Instant.now ();
                builder.setCreationTimestamp ( now );
                builder.setLocked ( true );
                builder.setModificationTimestamp ( now );
                final CopyOnWriteArrayList<ValidationMessage> msgs = new CopyOnWriteArrayList<> ();
                msgs.add ( new ValidationMessage ( "system", Severity.ERROR, ErrorChannel.this.errorMessage, Collections.emptySet () ) );
                builder.setValidationMessages ( msgs );
                return builder.build ();
            }

            @Override
            public Map<MetaKey, String> getProvidedMetaData ()
            {
                return Collections.emptyMap ();
            }

            @Override
            public SortedMap<MetaKey, String> getMetaData ()
            {
                return Collections.emptySortedMap ();
            }

            @Override
            public Map<MetaKey, String> getExtractedMetaData ()
            {
                return Collections.emptyMap ();
            }

            @Override
            public Map<MetaKey, CacheEntryInformation> getCacheEntries ()
            {
                return Collections.emptyMap ();
            }

            @Override
            public Map<String, ArtifactInformation> getArtifacts ()
            {
                return Collections.emptyMap ();
            }
        } ) );
    }
}
