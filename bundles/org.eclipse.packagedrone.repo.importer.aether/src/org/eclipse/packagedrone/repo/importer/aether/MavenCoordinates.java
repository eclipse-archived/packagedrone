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
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.packagedrone.utils.converter.JSON;

@JSON
public class MavenCoordinates implements Comparable<MavenCoordinates>
{
    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String extension;

    public MavenCoordinates ()
    {
    }

    public MavenCoordinates ( final MavenCoordinates other )
    {
        this.groupId = other.groupId;
        this.artifactId = other.artifactId;
        this.version = other.version;
        this.classifier = other.classifier;
        this.extension = other.extension;
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

    /**
     * Create a new instance without classifier and extension
     */
    public MavenCoordinates toBase ()
    {
        if ( this.classifier == null && this.extension == null )
        {
            return this;
        }

        return new MavenCoordinates ( this.groupId, this.artifactId, this.version );
    }

    public static MavenCoordinates fromString ( final String coords )
    {
        return fromArtifact ( new DefaultArtifact ( coords ) );
    }

    public static MavenCoordinates fromResult ( final ArtifactResult result )
    {
        if ( result == null )
        {
            return null;
        }

        return fromArtifact ( result.getRequest ().getArtifact () );
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

    @Override
    public int compareTo ( final MavenCoordinates o )
    {
        int rc;

        rc = compare ( this.groupId, o.groupId );
        if ( rc != 0 )
        {
            return rc;
        }
        rc = compare ( this.artifactId, o.artifactId );
        if ( rc != 0 )
        {
            return rc;
        }
        rc = compare ( this.version, o.version );
        if ( rc != 0 )
        {
            return rc;
        }
        rc = compare ( this.classifier, o.classifier );
        if ( rc != 0 )
        {
            return rc;
        }
        return compare ( this.extension, o.extension );
    }

    private int compare ( final String s1, final String s2 )
    {
        if ( s1 == s2 )
        {
            return 0;
        }

        if ( s1 == null )
        {
            return -1;
        }
        if ( s2 == null )
        {
            return 1;
        }
        return s1.compareTo ( s2 );
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.artifactId == null ? 0 : this.artifactId.hashCode () );
        result = prime * result + ( this.classifier == null ? 0 : this.classifier.hashCode () );
        result = prime * result + ( this.extension == null ? 0 : this.extension.hashCode () );
        result = prime * result + ( this.groupId == null ? 0 : this.groupId.hashCode () );
        result = prime * result + ( this.version == null ? 0 : this.version.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( ! ( obj instanceof MavenCoordinates ) )
        {
            return false;
        }
        final MavenCoordinates other = (MavenCoordinates)obj;
        if ( this.artifactId == null )
        {
            if ( other.artifactId != null )
            {
                return false;
            }
        }
        else if ( !this.artifactId.equals ( other.artifactId ) )
        {
            return false;
        }
        if ( this.classifier == null )
        {
            if ( other.classifier != null )
            {
                return false;
            }
        }
        else if ( !this.classifier.equals ( other.classifier ) )
        {
            return false;
        }
        if ( this.extension == null )
        {
            if ( other.extension != null )
            {
                return false;
            }
        }
        else if ( !this.extension.equals ( other.extension ) )
        {
            return false;
        }
        if ( this.groupId == null )
        {
            if ( other.groupId != null )
            {
                return false;
            }
        }
        else if ( !this.groupId.equals ( other.groupId ) )
        {
            return false;
        }
        if ( this.version == null )
        {
            if ( other.version != null )
            {
                return false;
            }
        }
        else if ( !this.version.equals ( other.version ) )
        {
            return false;
        }
        return true;
    }

}
