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
package org.eclipse.packagedrone.repo.channel.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntry;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.utils.IOConsumer;

public interface AccessContext
{
    public ChannelState getState ();

    public SortedMap<MetaKey, String> getMetaData ();

    public Map<String, ArtifactInformation> getArtifacts ();

    public boolean stream ( String artifactId, IOConsumer<InputStream> consumer ) throws IOException;

    public default boolean stream ( final ArtifactInformation artifact, final IOConsumer<InputStream> consumer ) throws IOException
    {
        return stream ( artifact.getId (), consumer );
    }

    public default SortedMap<String, String> getAspectStates ()
    {
        return Collections.emptySortedMap ();
    }

    public Map<MetaKey, CacheEntryInformation> getCacheEntries ();

    public boolean streamCacheEntry ( MetaKey key, IOConsumer<CacheEntry> consumer ) throws IOException;

    public Map<MetaKey, String> getProvidedMetaData ();

    public Map<MetaKey, String> getExtractedMetaData ();
}
