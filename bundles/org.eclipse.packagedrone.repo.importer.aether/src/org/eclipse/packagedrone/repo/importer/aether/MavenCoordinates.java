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
package org.eclipse.packagedrone.repo.importer.aether;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.packagedrone.utils.converter.JSON;

@JSON
public class MavenCoordinates
{
    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String extension;

    public MavenCoordinates ()
    {
    }

    public MavenCoordinates ( final String groupId, final String artifactId, final String version )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId ()
    {
        return this.groupId;
    }

    public void setGroupId ( final String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId ()
    {
        return this.artifactId;
    }

    public void setArtifactId ( final String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }

    public String getClassifier ()
    {
        return this.classifier;
    }

    public void setClassifier ( final String classifier )
    {
        this.classifier = classifier;
    }

    public String getExtension ()
    {
        return this.extension;
    }

    public void setExtension ( final String extension )
    {
        this.extension = extension;
    }

    public static MavenCoordinates fromResult ( final ArtifactResult result )
    {
        if ( result == null )
        {
            return null;
        }

        return fromArtifact ( result.getArtifact () );
    }

    public static MavenCoordinates fromArtifact ( final Artifact art )
    {
        if ( art == null )
        {
            return null;
        }

        final MavenCoordinates coords = new MavenCoordinates ();

        coords.setGroupId ( art.getGroupId () );
        coords.setArtifactId ( art.getArtifactId () );
        coords.setVersion ( art.getVersion () );
        coords.setClassifier ( art.getClassifier () );
        coords.setExtension ( art.getExtension () );

        return coords;
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( this.groupId );
        sb.append ( ':' ).append ( this.artifactId );

        if ( this.extension != null && !this.extension.isEmpty () )
        {
            sb.append ( ':' ).append ( this.extension );
        }
        if ( this.classifier != null && !this.classifier.isEmpty () )
        {
            sb.append ( ':' ).append ( this.classifier );
        }

        sb.append ( ':' ).append ( this.version );

        return sb.toString ();
    }
}
