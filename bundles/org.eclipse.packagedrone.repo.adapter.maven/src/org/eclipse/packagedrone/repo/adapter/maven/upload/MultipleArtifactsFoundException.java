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

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public class MultipleArtifactsFoundException extends ChecksumValidationException
{
    private static final long serialVersionUID = 1L;

    private final Coordinates coordinates;

    private final Collection<ArtifactInformation> artifacts;

    public MultipleArtifactsFoundException ( final Coordinates coordinates, final Collection<ArtifactInformation> artifacts )
    {
        super ( String.format ( "Multiple artifacts found for: %s -> %s", coordinates, artifacts.stream ().map ( ArtifactInformation::getId ).collect ( joining ( ", " ) ) ) );
        this.coordinates = coordinates;
        this.artifacts = artifacts;
    }

    public Coordinates getCoordinates ()
    {
        return this.coordinates;
    }

    public Collection<ArtifactInformation> getArtifacts ()
    {
        return this.artifacts;
    }
}
