/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S2162
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.maven;

import org.eclipse.packagedrone.repo.MetaKeyBinding;

public class MavenInformation
{
    @MetaKeyBinding ( namespace = "mvn", key = "groupId" )
    private String groupId;

    @MetaKeyBinding ( namespace = "mvn", key = "artifactId" )
    private String artifactId;

    @MetaKeyBinding ( namespace = "mvn", key = "version" )
    private String version;

    @MetaKeyBinding ( namespace = "mvn", key = "classifier" )
    private String classifier;

    @MetaKeyBinding ( namespace = "mvn", key = "extension" )
    private String extension;

    @MetaKeyBinding ( namespace = "mvn", key = "snapshotVersion" )
    private String snapshotVersion;

    @MetaKeyBinding ( namespace = "mvn", key = "buildNumber" )
    private Long buildNumber;

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

    public String getSnapshotVersion ()
    {
        return this.snapshotVersion;
    }

    public void setSnapshotVersion ( final String snapshotVersion )
    {
        this.snapshotVersion = snapshotVersion;
    }

    public void setBuildNumber ( final Long buildNumber )
    {
        this.buildNumber = buildNumber;
    }

    public Long getBuildNumber ()
    {
        return this.buildNumber;
    }

    public String makePath ()
    {
        final StringBuilder sb = new StringBuilder ();

        appendPath ( sb );
        sb.append ( '/' );
        appendFile ( sb, false );

        return sb.toString ();
    }

    public String makeName ()
    {
        final StringBuilder sb = new StringBuilder ();
        appendFile ( sb, false );
        return sb.toString ();
    }

    public String makePlainName ()
    {
        final StringBuilder sb = new StringBuilder ();
        appendFile ( sb, true );
        return sb.toString ();
    }

    /**
     * Check if this artifact should be the parent artifact
     *
     * @return <code>true</code> if the artifact should be a primary artifact,
     *         <code>false</code> otherwise
     */
    public boolean isPrimary ()
    {
        return this.classifier == null || this.classifier.isEmpty ();
    }

    protected void appendFile ( final StringBuilder sb, final boolean ignoreClassifier )
    {
        sb.append ( this.artifactId );
        sb.append ( '-' );

        if ( this.snapshotVersion != null )
        {
            sb.append ( this.snapshotVersion );
        }
        else
        {
            sb.append ( this.version );
        }

        if ( this.classifier != null && !ignoreClassifier )
        {
            sb.append ( '-' );
            sb.append ( this.classifier );
        }
        sb.append ( '.' );
        sb.append ( this.extension );
    }

    protected void appendPath ( final StringBuilder sb )
    {
        sb.append ( this.groupId );
        sb.append ( '/' );
        sb.append ( this.artifactId );
        sb.append ( '/' );
        sb.append ( this.version );
    }

    public boolean isSnapshot ()
    {
        return this.version != null && this.version.endsWith ( "-SNAPSHOT" );
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();
        sb.append ( this.groupId );
        sb.append ( '/' );
        appendFile ( sb, false );
        return sb.toString ();
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.artifactId == null ? 0 : this.artifactId.hashCode () );
        result = prime * result + ( this.buildNumber == null ? 0 : this.buildNumber.hashCode () );
        result = prime * result + ( this.classifier == null ? 0 : this.classifier.hashCode () );
        result = prime * result + ( this.extension == null ? 0 : this.extension.hashCode () );
        result = prime * result + ( this.groupId == null ? 0 : this.groupId.hashCode () );
        result = prime * result + ( this.snapshotVersion == null ? 0 : this.snapshotVersion.hashCode () );
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
        if ( this.getClass() != obj.getClass() )
        {
            return false;
        }
        final MavenInformation other = (MavenInformation)obj;
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
        if ( this.buildNumber == null )
        {
            if ( other.buildNumber != null )
            {
                return false;
            }
        }
        else if ( !this.buildNumber.equals ( other.buildNumber ) )
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
        if ( this.snapshotVersion == null )
        {
            if ( other.snapshotVersion != null )
            {
                return false;
            }
        }
        else if ( !this.snapshotVersion.equals ( other.snapshotVersion ) )
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
