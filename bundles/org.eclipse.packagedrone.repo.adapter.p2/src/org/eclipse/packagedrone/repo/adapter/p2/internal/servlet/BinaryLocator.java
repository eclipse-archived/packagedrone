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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.packagedrone.repo.adapter.p2.aspect.P2RepoConstants;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;

public final class BinaryLocator
{
    private BinaryLocator ()
    {
    }

    private static List<ArtifactInformation> filterByMaven ( final ReadableChannel channel, final String id, final String version )
    {
        // all artifacts
        final Collection<ArtifactInformation> arts = channel.getArtifacts ();

        // get a stream to the artifacts which are p2 artifacts fragments and provide this key
        Stream<ArtifactInformation> matchingArts = findByKeys ( arts, "binary", id, version );

        // from the list of p2 fragments, find the matching maven artifact
        matchingArts = matchingArts.flatMap ( art -> {

            Collection<ArtifactInformation> candidates;
            final String parentId = art.getParentId ();

            if ( parentId != null )
            {
                candidates = channel.getChildrenOf ( parentId ).orElse ( Collections.emptyList () );
            }
            else
            {
                candidates = arts;
            }

            if ( candidates == null )
            {
                return Stream.empty ();
            }

            final String groupId = MavenLocator.getGroupId ( art );
            final String artifactId = MavenLocator.getArtifactId ( art );
            final String mavenVersion = MavenLocator.getVersion ( art );
            final String snapshotVersion = MavenLocator.getSnapshotVersion ( art );
            final String extension = MavenLocator.getExtension ( art );
            final String classifier = MavenLocator.getClassifier ( art );

            return MavenLocator.filterByCoordinates ( candidates.stream (), groupId, artifactId, mavenVersion, snapshotVersion, extension, classifier );
        } );

        return matchingArts.collect ( Collectors.toList () );
    }

    public static Optional<ArtifactInformation> findByMaven ( final ReadableChannel channel, final String id, final String version )
    {
        final List<ArtifactInformation> result = filterByMaven ( channel, id, version );
        if ( result.isEmpty () )
        {
            return Optional.empty ();
        }

        Collections.sort ( result, Comparator.comparing ( ArtifactInformation::getCreationInstant ) );

        // get the first one

        return Optional.ofNullable ( result.get ( 0 ) );
    }

    private static Stream<ArtifactInformation> findByKeys ( final Collection<ArtifactInformation> artifacts, final String classifier, final String id, final String version )
    {
        final String key = String.format ( "%s::%s::%s", classifier, id, version );
        return artifacts.stream ().filter ( art -> hasKey ( art, key ) );
    }

    /**
     * Check if the {@code p2.repo:fragment-keys} field contains a matching key
     *
     * @param art
     *            the artifact to check
     * @param key
     *            the required key value
     * @return {@code true} if the artifact has a matching key entry,
     *         {@code false} otherwise
     */
    private static boolean hasKey ( final ArtifactInformation art, final String key )
    {
        if ( key == null )
        {
            return false;
        }

        final String keysString = art.getMetaData ().get ( P2RepoConstants.KEY_FRAGMENT_KEYS );
        if ( keysString == null )
        {
            return false;
        }

        final String[] keys = keysString.split ( P2RepoConstants.ENTRY_DELIMITER );
        for ( final String actualKey : keys )
        {
            if ( key.equals ( actualKey ) )
            {
                return true;
            }
        }
        return false;
    }
}
