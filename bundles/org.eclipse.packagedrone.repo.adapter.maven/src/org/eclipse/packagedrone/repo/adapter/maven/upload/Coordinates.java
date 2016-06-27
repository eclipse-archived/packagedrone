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
package org.eclipse.packagedrone.repo.adapter.maven.upload;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maven coordinates for the uploader
 */
public class Coordinates
{
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String qualifiedVersion;

    private final String classifier;

    private final String extension;

    public Coordinates ( final String groupId, final String artifactId, final String version, final String extension )
    {
        this ( groupId, artifactId, version, version, null, extension );
    }

    public Coordinates ( final String groupId, final String artifactId, final String version, final String classifier, final String extension )
    {
        this ( groupId, artifactId, version, version, classifier, extension );
    }

    public Coordinates ( final String groupId, final String artifactId, final String version, final String qualifiedVersion, final String classifier, final String extension )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.qualifiedVersion = qualifiedVersion;
        this.classifier = classifier;
        this.extension = extension;
    }

    public String getGroupId ()
    {
        return this.groupId;
    }

    public String getArtifactId ()
    {
        return this.artifactId;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public String getQualifiedVersion ()
    {
        return this.qualifiedVersion;
    }

    public String getClassifier ()
    {
        return this.classifier;
    }

    public String getExtension ()
    {
        return this.extension;
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
        result = prime * result + ( this.qualifiedVersion == null ? 0 : this.qualifiedVersion.hashCode () );
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
        final Coordinates other = (Coordinates)obj;
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
        if ( this.qualifiedVersion == null )
        {
            if ( other.qualifiedVersion != null )
            {
                return false;
            }
        }
        else if ( !this.qualifiedVersion.equals ( other.qualifiedVersion ) )
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

    @Override
    public String toString ()
    {
        return toFullPath ();
    }

    public String toFullPath ()
    {
        final StringBuilder sb = new StringBuilder ();
        appendFullPath ( sb );
        return sb.toString ();
    }

    public String toFileName ()
    {
        final StringBuilder sb = new StringBuilder ();
        appendFileName ( sb );
        return sb.toString ();
    }

    public static String makePath ( final String groupId, final String artifactId, final String version, final String qualifiedVersion, final String classifier, final String extension )
    {
        return new Coordinates ( groupId, artifactId, version, qualifiedVersion, classifier, extension ).toFullPath ();
    }

    public void appendFullPath ( final StringBuilder sb )
    {
        sb.append ( '/' );
        sb.append ( this.groupId.replace ( '.', '/' ) );

        sb.append ( '/' );
        sb.append ( this.artifactId );

        sb.append ( '/' );
        sb.append ( this.version );

        sb.append ( '/' );
        appendFileName ( sb );
    }

    public void appendFileName ( final StringBuilder sb )
    {
        sb.append ( this.artifactId ).append ( '-' ).append ( this.qualifiedVersion == null ? this.version : this.qualifiedVersion );
        if ( this.classifier != null )
        {
            sb.append ( '-' ).append ( this.classifier );
        }
        sb.append ( '.' ).append ( this.extension );
    }

    public static Coordinates parse ( final String path )
    {
        final LinkedList<String> toks = new LinkedList<> ( Arrays.asList ( path.split ( "/" ) ) );

        final String fileName = toks.pollLast ();
        final String version = toks.pollLast ();
        final String artifactId = toks.pollLast ();

        final StringBuilder sb = new StringBuilder ();
        for ( final String tok : toks )
        {
            if ( sb.length () > 0 )
            {
                sb.append ( '.' );
            }
            sb.append ( tok );
        }
        final String groupId = sb.toString ();

        for ( final String expectedVersion : makeExpectedVersions ( version ) )
        {
            final String[] split = extractInformation ( artifactId, expectedVersion, fileName );
            if ( split != null )
            {
                return new Coordinates ( groupId, artifactId, version, split[0], split[1], split[2] );
            }
        }

        return null;
    }

    public static boolean isSnapshot ( final String version )
    {
        return version.endsWith ( SNAPSHOT_SUFFIX );
    }

    private static String[] makeExpectedVersions ( final String version )
    {
        if ( !isSnapshot ( version ) )
        {
            return new String[] { Pattern.quote ( version ) };
        }

        final String baseVersion = version.substring ( 0, version.length () - SNAPSHOT_SUFFIX.length () );

        return new String[] { Pattern.quote ( version ), Pattern.quote ( baseVersion ) + "-\\d{8}\\.\\d{6}-\\d+" };
    }

    private static String[] extractInformation ( final String artifactId, final String expectedVersionPattern, final String name )
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( Pattern.quote ( artifactId ) );
        sb.append ( "-" );
        sb.append ( "(?<v>" + expectedVersionPattern + ")" );
        sb.append ( "(|-(?<cl>[^\\.]+))" );
        sb.append ( "(|\\.(?<ext>.*+))" );

        final Pattern p = Pattern.compile ( sb.toString () );

        final Matcher m = p.matcher ( name );
        if ( m.matches () )
        {
            final String version = m.group ( "v" );
            final String classifier = m.group ( "cl" );
            final String extension = m.group ( "ext" );

            return new String[] { version, classifier, extension };
        }
        return null;
    }

    /**
     * Create an unclassified version of ourself
     *
     * @return either a new instance without a classifier, or the same instance
     *         if it already is without a classifier
     */
    public Coordinates makeUnclassified ()
    {
        if ( this.classifier == null )
        {
            return this;
        }

        return new Coordinates ( this.groupId, this.artifactId, this.version, this.qualifiedVersion, null, this.extension );
    }

    public Coordinates replaceExtension ( final String newExtension )
    {
        return new Coordinates ( this.groupId, this.artifactId, this.version, this.qualifiedVersion, this.classifier, newExtension );
    }
}
