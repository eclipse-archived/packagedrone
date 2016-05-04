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
package org.eclipse.packagedrone.repo.channel.provider;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;

/**
 * A direct access to the channel
 * <p>
 * This is a more direct access to the channel than using the
 * {@link ModifiableChannel} interface. It may allow more operations, but the
 * API is considered less stable. If possible it is preferred to use the
 * {@link ModifiableChannel} interface.
 * </p>
 * <h2>Artifact creation</h2>
 * <p>
 * Artifacts can be manually created using the {@code createArtifact} methods.
 * This will result in creating a plain "stored" artifact. Some aspects do
 * create artifacts automatically, based on the channel state, those artifacts
 * are "generated" or "virtual" artifacts, which cannot be deleted or be a
 * parent of other "stored" artifacts.
 * </p>
 * <p>
 * "generator" artifacts can be created using the
 * {@link #createGeneratorArtifact(String, InputStream, String, Map)} method.
 * Those artifacts are primarily used for creating other "generated" artifacts
 * based on the channel state or their artifact payload. A "generator" artifact
 * does not need to have any content though and may be created without.
 */
public interface ModifyContext extends AccessContext
{
    public void applyMetaData ( Map<MetaKey, String> changes );

    public void applyMetaData ( String artifactId, Map<MetaKey, String> changes );

    public void lock ();

    public void unlock ();

    /**
     * Create a new root level artifact
     * <p>
     * Performs a call to
     * {@link #createArtifact(String, InputStream, String, Map)} with a parent
     * id of {@code null}
     * </p>
     * <p>
     * <strong>Note:</strong> the caller is responsible for closing the provided
     * input source.
     * </p>
     *
     * @param source
     *            the source of input data, must not be {@code null}
     * @param name
     *            the name of the artifact
     * @param providedMetaData
     *            the initial provided meta data
     * @return the artifact information of the newly created artifact, or
     *         {@code null} if the artifact was not created
     * @throws RuntimeException
     *             if the creation of the artifact failed
     */
    public default ArtifactInformation createArtifact ( final InputStream source, final String name, final Map<MetaKey, String> providedMetaData )
    {
        return createArtifact ( null, source, name, providedMetaData );
    }

    /**
     * Create a new root level artifact
     * <p>
     * <strong>Note:</strong> the caller is responsible for closing the provided
     * input source.
     * </p>
     *
     * @param parentId
     *            optionally the id of the parent artifact, if specified the
     *            parent artifact must be a "stored" artifact, created by a
     *            previous call to
     *            {@link #createArtifact(String, InputStream, String, Map)}.
     * @param source
     *            the source of input data, must not be {@code null}
     * @param name
     *            the name of the artifact
     * @param providedMetaData
     *            the initial provided meta data
     * @return the artifact information of the newly created artifact, or
     *         {@code null} if the artifact was not created
     * @throws RuntimeException
     *             if the creation of the artifact failed
     */
    public ArtifactInformation createArtifact ( final String parentId, final InputStream source, final String name, final Map<MetaKey, String> providedMetaData );

    /**
     * Create a new generator artifact
     *
     * @param generatorId
     *            the ID of the generator implementation
     * @param source
     *            the source of the input data, may be {@code null}
     * @param name
     *            the name of the artifact
     * @param providedMetaData
     *            the initial provided meta data
     * @return the artifact information of the newly created artifact, or
     *         {@code null} if the artifact was not created
     * @throws RuntimeException
     *             if the creation of the artifact failed
     */
    public ArtifactInformation createGeneratorArtifact ( String generatorId, InputStream source, String name, Map<MetaKey, String> providedMetaData );

    /**
     * Delete one artifact
     *
     * @param artifactId
     *            the id of the artifact to delete
     * @return <code>
     */
    public default boolean deleteArtifact ( final String artifactId )
    {
        return deleteArtifacts ( Collections.singleton ( artifactId ) ) == 1;
    }

    /**
     * Delete artifacts from a channel
     *
     * @param artifactIds
     *            the artifacts to delete
     * @return the number of artifacts deleted from the input set
     */
    public int deleteArtifacts ( Set<String> artifactIds );

    /**
     * Remove all artifacts from the channel
     */
    public void clear ();

    public void addAspects ( Set<String> aspectIds );

    public void removeAspects ( Set<String> aspectIds );

    /**
     * Refresh the provided aspects
     * <p>
     * Passing in an empty or {@code null} set of aspect is, will cause
     * <em>all</em> aspects to be refreshed.
     * </p>
     *
     * @param aspectIds
     *            The IDs of the aspects to refresh. Maybe {@code null} or
     *            empty, in which case all aspects will be refreshed.
     */
    public void refreshAspects ( Set<String> aspectIds );

    /**
     * Re-generate a generator artifact
     *
     * @param artifactId
     *            the id of the generator artifact
     */
    public void regenerate ( String artifactId );
}
