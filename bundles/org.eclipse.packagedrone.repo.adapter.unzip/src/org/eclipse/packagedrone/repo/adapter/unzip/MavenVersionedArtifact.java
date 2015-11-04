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
package org.eclipse.packagedrone.repo.adapter.unzip;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public class MavenVersionedArtifact implements Comparable<MavenVersionedArtifact>
{
    private final ComparableVersion version;

    private final String channelId;

    private final ArtifactInformation artifact;

    public MavenVersionedArtifact ( final ComparableVersion version, final String channelId, final ArtifactInformation artifact )
    {
        this.version = version;
        this.channelId = channelId;
        this.artifact = artifact;
    }

    public ComparableVersion getVersion ()
    {
        return this.version;
    }

    public String getChannelId ()
    {
        return this.channelId;
    }

    public ArtifactInformation getArtifact ()
    {
        return this.artifact;
    }

    @Override
    public int compareTo ( final MavenVersionedArtifact o )
    {
        return this.version.compareTo ( o.version );
    }
}
