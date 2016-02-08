/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.utils.io.IOConsumer;

public interface ArtifactContext
{
    /**
     * Get the information of the artifact which is currently being processed
     *
     * @return the artifact information of the current artifact, never returns
     *         <code>null</code>
     */
    public ArtifactInformation getArtifactInformation ();

    /**
     * Get the path to the temporary file holding the BLOB
     *
     * @return a path to the temporary BLOB data
     */
    public Path getFile ();

    /**
     * Create a new virtual artifact in the current context
     *
     * @param name
     *            the name of the artifact
     * @param stream
     *            the receiver of an output stream where the content can be
     *            written to
     * @param providedMetaData
     *            the provided meta data, may be <code>null</code>
     */
    public void createVirtualArtifact ( String name, IOConsumer<OutputStream> stream, Map<MetaKey, String> providedMetaData );

    /**
     * Get information on any other artifact
     *
     * @param artifactId
     *            the ID of the artifact to fetch information for
     * @return the artifact information
     */
    public ArtifactInformation getOtherArtifactInformation ( String artifactId );

    /**
     * Get the provided channel meta data
     * <p>
     * This call only returns the provided channel meta data, since the
     * extracted (aggregated) channel meta data will only be available after the
     * virtualizer or generator have run an cannot be used therefore.
     * </p>
     *
     * @return a map with the meta data, never returns <code>null</code>
     */
    public Map<MetaKey, String> getProvidedChannelMetaData ();
}
