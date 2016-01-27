/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.p2.internal.servlet;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public final class MavenLocator
{
    public static final MetaKey KEY_GROUP_ID = new MetaKey ( "mvn", "groupId" );

    public static final MetaKey KEY_ARTIFACT_ID = new MetaKey ( "mvn", "artifactId" );

    public static final MetaKey KEY_VERSION = new MetaKey ( "mvn", "version" );

    public static final MetaKey KEY_SNAPSHOT_VERSION = new MetaKey ( "mvn", "snapshotVersion" );

    public static final MetaKey KEY_EXTENSION = new MetaKey ( "mvn", "extension" );

    public static final MetaKey KEY_CLASSIFIER = new MetaKey ( "mvn", "classifier" );

    private MavenLocator ()
    {
    }

    public static List<ArtifactInformation> findByMavenCoordinates ( final Collection<ArtifactInformation> artifacts, final String groupId, final String artifactId, final String version, final String snapshotVersion, final String extension, final String classifier )
    {
        final Stream<ArtifactInformation> s = filterByCoordinates ( artifacts.stream (), groupId, artifactId, version, snapshotVersion, extension, classifier );
        return s.collect ( Collectors.toList () );
    }

    public static <T extends ArtifactInformation> Stream<T> filterByCoordinates ( Stream<T> s, final String groupId, final String artifactId, final String version, final String snapshotVersion, final String extension, final String classifier )
    {
        s = s.filter ( art -> has ( art, KEY_GROUP_ID, groupId ) );
        s = s.filter ( art -> has ( art, KEY_ARTIFACT_ID, artifactId ) );
        s = s.filter ( art -> has ( art, KEY_VERSION, version ) );
        s = s.filter ( art -> has ( art, KEY_SNAPSHOT_VERSION, snapshotVersion ) );
        s = s.filter ( art -> has ( art, KEY_EXTENSION, extension ) );
        s = s.filter ( art -> has ( art, KEY_CLASSIFIER, classifier ) );
        return s;
    }

    private static boolean has ( final ArtifactInformation art, final MetaKey key, final String expectedValue )
    {
        final String actualValue = art.getMetaData ().get ( key );
        if ( actualValue == expectedValue )
        {
            // covers two nulls
            return true;
        }

        if ( actualValue == null )
        {
            // both null would have returned true earlier
            return false;
        }

        return actualValue.equals ( expectedValue );
    }

    private static String get ( final ArtifactInformation art, final MetaKey key )
    {
        Objects.requireNonNull ( art );
        return art.getMetaData ().get ( key );
    }

    public static String getGroupId ( final ArtifactInformation art )
    {
        return get ( art, KEY_GROUP_ID );
    }

    public static String getArtifactId ( final ArtifactInformation art )
    {
        return get ( art, KEY_ARTIFACT_ID );
    }

    public static String getVersion ( final ArtifactInformation art )
    {
        return get ( art, KEY_VERSION );
    }

    public static String getSnapshotVersion ( final ArtifactInformation art )
    {
        return get ( art, KEY_SNAPSHOT_VERSION );
    }

    public static String getExtension ( final ArtifactInformation art )
    {
        return get ( art, KEY_EXTENSION );
    }

    public static String getClassifier ( final ArtifactInformation art )
    {
        return get ( art, KEY_CLASSIFIER );
    }

}
