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
package org.eclipse.packagedrone.repo.aspect.aggregate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService.ArtifactReceiver;
import org.eclipse.packagedrone.utils.io.IOConsumer;

import com.google.common.io.ByteStreams;

public interface AggregationContext extends AggregationValidationContext
{
    public Collection<ArtifactInformation> getArtifacts ();

    /**
     * Get the external channel id
     * 
     * @return the external channel id
     */
    public String getChannelId ();

    public String getChannelDescription ();

    /**
     * Get the provided channel meta data
     *
     * @return the provided channel meta data
     */
    public Map<MetaKey, String> getChannelMetaData ();

    /**
     * Create a persisted cache entry
     *
     * @param id
     *            the id of the cache entry, must be unique for this aspect and
     *            channel
     * @param stream
     *            the stream providing the data for the cache entry
     * @throws IOException
     */
    public default void createCacheEntry ( final String id, final String name, final String mimeType, final InputStream stream ) throws IOException
    {
        createCacheEntry ( id, name, mimeType, ( output ) -> ByteStreams.copy ( stream, output ) );
    }

    /**
     * Create a persisted cache entry
     * <p>
     * This variant allows one to provide a function which will get called with
     * an output stream, to which the content can be written.
     * </p>
     *
     * @param id
     *            the id of the cache entry, must be unique for this aspect and
     *            channel
     * @param creator
     *            the content creator
     * @throws IOException
     */
    public void createCacheEntry ( String id, String name, String mimeType, IOConsumer<OutputStream> creator ) throws IOException;

    public boolean streamArtifact ( String artifactId, ArtifactReceiver receiver ) throws IOException;

    public default boolean streamArtifact ( final String artifactId, final IOConsumer<InputStream> consumer ) throws IOException
    {
        return streamArtifact ( artifactId, ( artifact, stream ) -> consumer.accept ( stream ) );
    }

}
