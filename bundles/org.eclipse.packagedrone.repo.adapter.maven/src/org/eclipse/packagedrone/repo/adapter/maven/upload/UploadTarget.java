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
package org.eclipse.packagedrone.repo.adapter.maven.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.MetaKey;

/**
 * A target for the {@link Uploader}
 */
public interface UploadTarget
{
    public default String createArtifact ( final Coordinates coordinates, final InputStream stream, final Map<MetaKey, String> metaData ) throws IOException
    {
        return createArtifact ( null, coordinates, stream, metaData );
    }

    /**
     * Create a new artifact
     *
     * @param parentId
     *            an optional parent id
     * @param coordinates
     *            the coordinates to create the artifact for
     * @param stream
     *            the data stream
     * @param metaData
     *            the provided meta data, these already contain the converted
     *            coordinate information
     * @return the id of the artifact which was created, or {@code null} if the
     *         artifact was rejected and not stored (which is not an error)
     * @throws IOException
     *             if there was an IO error during the storage process
     */
    public String createArtifact ( String parentId, Coordinates coordinates, InputStream stream, Map<MetaKey, String> metaData ) throws IOException;

    /**
     * Find all artifacts which match the provided coordinates
     *
     * @param coordinates
     *            the coordinates to look for
     * @return the set of artifact ids which are considered a perfect match for
     *         these coordinates. Never returns {@code null}.
     */
    public Set<String> findArtifacts ( Coordinates coordinates );

    /**
     * Validate a checksum
     * <p>
     * This method should validate the checksum of an artifact. If the checksum
     * is considered invalid this method must throw an
     * {@link ChecksumValidationException}.
     * </p>
     * <p>
     * The checksum must be considered invalid if:
     * </p>
     * <ul>
     * <li>The artifact is found and the value of the checksum is not the same
     * (case insensitive)</li>
     * <li>The artifact is not found</li>
     * </ul>
     * The checksum should not be considered if the artifact is found, but the
     * checksum algorithm is not understood.
     *
     * @param coordinates
     *            the coordinates of the artifact to check
     * @param algorithm
     *            the checksum algorithm
     * @param value
     *            the hex encoded checksum
     * @throws ChecksumValidationException
     *             if the checksum is invalid
     */
    public void validateChecksum ( Coordinates coordinates, String algorithm, String value ) throws ChecksumValidationException;
}
