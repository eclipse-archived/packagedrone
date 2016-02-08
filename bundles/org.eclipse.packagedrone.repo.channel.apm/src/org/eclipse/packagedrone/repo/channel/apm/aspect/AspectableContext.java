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
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.utils.io.IOConsumer;

public interface AspectableContext
{
    public interface ArtifactAddition
    {
        /**
         * The final artifact ID, this value is not going to change.
         * 
         * @return the artifact ID
         */
        public String getId ();

        public String getName ();

        public Instant getCreationTimestamp ();

        /**
         * Actually create the artifact
         * <p>
         * This will stream and store the binary data to the repository. It
         * still can be rolled back by the outer transaction.
         * </p>
         * <p>
         * <strong>Note: </strong> This method may only be called once per
         * instance. Adding another artifacts requires to prepare a new artifact
         * using
         * {@link AspectableContext#preparePlainArtifact(String, String, Map, Set, String)}
         * . Overwriting binary content is not allowed.
         * </p>
         *
         * @param stream
         *            A stream to the content to be added
         * @return the final artifact information entry
         */
        public ArtifactInformation create ( InputStream stream );
    }

    public String getChannelId ();

    public SortedMap<String, String> getModifiableAspectStates ();

    public ArtifactAddition preparePlainArtifact ( String parentArtifactId, String name, Map<MetaKey, String> providedMetaData, Set<String> facets, String virtualizerAspectId );

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
}
