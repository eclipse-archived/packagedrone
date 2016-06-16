/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.provider.AccessContext;
import org.eclipse.packagedrone.repo.channel.search.ArtifactLocator;
import org.eclipse.packagedrone.repo.channel.search.stream.StreamArtifactLocator;
import org.eclipse.packagedrone.utils.io.IOConsumer;

public interface ReadableChannel
{
    public ChannelId getId ();

    public AccessContext getContext ();

    public default ChannelInformation getInformation ()
    {
        final AccessContext ctx = getContext ();
        return new ChannelInformation ( getId (), ctx.getState (), ctx.getMetaData (), ctx.getAspectStates () );
    }

    public default ChannelArtifactInformation withChannel ( final ArtifactInformation artifact )
    {
        if ( artifact == null )
        {
            return null;
        }
        return new ChannelArtifactInformation ( getId (), artifact );
    }

    public default Optional<ChannelArtifactInformation> getArtifact ( final String id )
    {
        return Optional.ofNullable ( withChannel ( getContext ().getArtifacts ().get ( id ) ) );
    }

    public default Optional<Collection<ArtifactInformation>> getChildrenOf ( final String artifactId )
    {
        final AccessContext ctx = getContext ();

        final ArtifactInformation art = ctx.getArtifacts ().get ( artifactId );
        if ( art == null )
        {
            return Optional.empty ();
        }

        return Optional.of ( art.getChildIds ().stream ().map ( childId -> ctx.getArtifacts ().get ( childId ) ).collect ( Collectors.toList () ) );
    }

    public default boolean hasAspect ( final String aspectId )
    {
        return getInformation ().getAspectStates ().containsKey ( aspectId );
    }

    public default Map<MetaKey, String> getMetaData ()
    {
        return getContext ().getMetaData ();
    }

    public default Map<MetaKey, CacheEntryInformation> getCacheEntries ()
    {
        return getContext ().getCacheEntries ();
    }

    public default boolean streamCacheEntry ( final MetaKey key, final IOConsumer<CacheEntry> consumer ) throws IOException
    {
        return getContext ().streamCacheEntry ( key, consumer );
    }

    public default Collection<ArtifactInformation> getArtifacts ()
    {
        return getContext ().getArtifacts ().values ();
    }

    public default List<ArtifactInformation> findByName ( final String name )
    {
        return getContext ().getArtifacts ().values ().stream ().filter ( art -> art.getName ().equals ( name ) ).collect ( Collectors.toList () );
    }

    public default ArtifactLocator getArtifactLocator ()
    {
        return new StreamArtifactLocator ( getArtifacts ()::stream );
    }
}
