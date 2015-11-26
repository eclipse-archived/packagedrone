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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.utils.io.IOConsumer;

public interface AspectableContext
{
    public SortedMap<String, String> getModifiableAspectStates ();

    public ArtifactInformation createPlainArtifact ( String parentArtifactId, InputStream source, String name, Map<MetaKey, String> providedMetaData, Set<String> facets, String virtualizerAspectId );

    public ArtifactInformation deletePlainArtifact ( String artifactId );

    public boolean stream ( String artifactId, IOConsumer<InputStream> consumer ) throws IOException;

    public ArtifactInformation setExtractedMetaData ( String artifactId, Map<MetaKey, String> metaData );

    public ArtifactInformation setValidationMessages ( String artifactId, List<ValidationMessage> messages );

    public void setExtractedMetaData ( Map<MetaKey, String> metaData );

    public void setValidationMessages ( List<ValidationMessage> messages );

    public Collection<ValidationMessage> getValidationMessages ();

    public Map<String, ArtifactInformation> getArtifacts ();

    public Map<String, ArtifactInformation> getGeneratorArtifacts ();

    public Map<MetaKey, String> getChannelProvidedMetaData ();

    public void createCacheEntry ( MetaKey metaKey, String name, String mimeType, IOConsumer<OutputStream> creator ) throws IOException;

    /**
     * Get the external channel id
     *
     * @return the external channel id
     */
    public String getChannelId ();

    /**
     * Get the channel details
     * <p>
     * <em>Note: </em> Although this is a bean with setter, the caller actually
     * gets a copy which does not get persisted.
     * </p>
     *
     * @return a fresh copy of the channel details
     */
    public ChannelDetails getChannelDetails ();

}
