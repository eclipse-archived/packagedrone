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
package org.eclipse.packagedrone.repo.adapter.maven.internal;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.adapter.maven.MavenInformation;
import org.eclipse.packagedrone.repo.adapter.maven.internal.MavenRepositoryChannelAggregator;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.junit.Before;
import org.junit.Test;

public class RepositoryAggregatorTest
{
    private Instant now;

    @Before
    public void setup ()
    {
        this.now = Instant.now ();
    }

    /**
     * This test checks a plain jar file with attached maven coordinates
     */
    @Test
    public void test1PlainJar ()
    {
        final ArtifactInformation a1 = makeArtifact ( "a1", null, "test-1.0.0.jar", "g1", "a1", "1.0.0", "jar", null, null );

        final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a1, makeMap ( a1 ) );

        assertEquals ( 1, result.size () );

        assertInfo ( "g1", "a1", "1.0.0", "jar", null, result.iterator ().next () );
    }

    /**
     * This tests checks an artifact without any maven coordinates
     */
    @Test
    public void test2PlainJarWithAdditionalNonMaven ()
    {
        final ArtifactInformation a1 = makeArtifact ( "a1", null, "foobar", null );

        final Map<String, ArtifactInformation> map = makeMap ( a1 );

        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a1, map );

            assertEquals ( 0, result.size () );
        }
    }

    /**
     * This test checks two maven artifacts, jar and pom, which are stored as
     * siblings. Each artifact has its own maven coordinates.
     */
    @Test
    public void test3JarAndPom ()
    {
        final ArtifactInformation a1 = makeArtifact ( "a1", null, "test-1.0.0.jar", "g1", "a1", "1.0.0", "jar", null, null );
        final ArtifactInformation a2 = makeArtifact ( "a2", null, "test-1.0.0.pom", "g1", "a1", "1.0.0", "pom", null, null );

        final Map<String, ArtifactInformation> map = makeMap ( a1, a2 );

        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a1, map );

            assertEquals ( 1, result.size () );

            assertInfo ( "g1", "a1", "1.0.0", "jar", null, result.iterator ().next () );
        }
        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a2, map );

            assertEquals ( 1, result.size () );

            assertInfo ( "g1", "a1", "1.0.0", "pom", null, result.iterator ().next () );
        }
    }

    /**
     * This test checks a jar with a sub pom file. Both artifacts have maven
     * coordinates.
     * <p>
     * This can happen if the a1 artifact is uploaded by maven and still the
     * maven pom is extracted. Which should lead in fact to the same maven
     * coordinates twice.
     * </p>
     */
    @Test
    public void test4JarAndSubPom ()
    {
        final ArtifactInformation a1 = makeArtifact ( "a1", null, "test-1.0.0.jar", "g1", "a1", "1.0.0", "jar", null, new String[] { "a2" } );
        final ArtifactInformation a2 = makeArtifact ( "a2", "a1", "test-1.0.0.pom", "g1", "a1", "1.0.0", "pom", null, null );

        final Map<String, ArtifactInformation> map = makeMap ( a1, a2 );

        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a1, map );

            assertEquals ( 1, result.size () );

            assertInfo ( "g1", "a1", "1.0.0", "jar", null, result.iterator ().next () );
        }
    }

    @Test
    public void test5PlainJarAndSubPom ()
    {
        final ArtifactInformation a1 = makeArtifact ( "a1", null, "test-1.0.0.jar", new String[] { "a2" } );
        final ArtifactInformation a2 = makeArtifact ( "a2", "a1", "test-1.0.0.pom", "g1", "a1", "1.0.0", "pom", null, null );

        final Map<String, ArtifactInformation> map = makeMap ( a1, a2 );

        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a1, map );

            assertEquals ( 1, result.size () );

            assertInfo ( "g1", "a1", "1.0.0", "jar", null, result.iterator ().next () );
        }
        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a2, map );

            assertEquals ( 1, result.size () );

            assertInfo ( "g1", "a1", "1.0.0", "pom", null, result.iterator ().next () );
        }
    }

    @Test
    public void test6PlainJarAndDoubleSubPom ()
    {
        final ArtifactInformation a1 = makeArtifact ( "a1", null, "test-1.0.0.jar", new String[] { "a2", "a3" } );
        final ArtifactInformation a2 = makeArtifact ( "a2", "a1", "test-1.0.0.pom", "g1", "a1", "1.0.0", "pom", null, null );
        final ArtifactInformation a3 = makeArtifact ( "a3", "a1", "test-1.0.0.pom", "g2", "a1", "1.0.0", "pom", null, null );

        final Map<String, ArtifactInformation> map = makeMap ( a1, a2, a3 );

        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a1, map );

            assertEquals ( 2, result.size () );

            final Iterator<MavenInformation> i = result.iterator ();
            assertInfo ( "g1", "a1", "1.0.0", "jar", null, i.next () );
            assertInfo ( "g2", "a1", "1.0.0", "jar", null, i.next () );
        }

        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a2, map );

            assertEquals ( 1, result.size () );

            assertInfo ( "g1", "a1", "1.0.0", "pom", null, result.iterator ().next () );
        }

        {
            final Collection<MavenInformation> result = MavenRepositoryChannelAggregator.getInfos ( a3, map );

            assertEquals ( 1, result.size () );

            assertInfo ( "g2", "a1", "1.0.0", "pom", null, result.iterator ().next () );
        }
    }

    private void assertInfo ( final String groupId, final String artifactId, final String version, final String extension, final String classifier, final MavenInformation info )
    {
        assertEquals ( groupId, info.getGroupId () );
        assertEquals ( artifactId, info.getArtifactId () );
        assertEquals ( version, info.getVersion () );
        assertEquals ( extension, info.getExtension () );
        assertEquals ( classifier, info.getClassifier () );
    }

    private Map<String, ArtifactInformation> makeMap ( final ArtifactInformation... arts )
    {
        return MavenRepositoryChannelAggregator.makeMap ( Arrays.asList ( arts ) );
    }

    private ArtifactInformation makeArtifact ( final String id, final String parentId, final String name, final String[] childIds )
    {
        return makeArtifact ( id, parentId, name, null, null, null, null, null, childIds );
    }

    private ArtifactInformation makeArtifact ( final String id, final String parentId, final String name, final String groupId, final String artifactId, final String version, final String extension, final String classifier, final String[] childIds )
    {
        final SortedMap<MetaKey, String> md = makeMavenCoords ( groupId, artifactId, version, extension, classifier );
        final Set<String> childIdSet = childIds == null ? Collections.emptySortedSet () : new TreeSet<> ( Arrays.asList ( childIds ) );

        return new ArtifactInformation ( id, parentId, childIdSet, name, 0L, this.now, Collections.emptySet (), Collections.emptyList (), md, null, null );
    }

    private SortedMap<MetaKey, String> makeMavenCoords ( final String groupId, final String artifactId, final String version, final String extension, final String classifier )
    {
        if ( groupId == null || artifactId == null || version == null )
        {
            return Collections.emptySortedMap ();
        }

        final MavenInformation mi = new MavenInformation ();

        mi.setGroupId ( groupId );
        mi.setArtifactId ( artifactId );
        mi.setVersion ( version );
        mi.setExtension ( extension );
        mi.setClassifier ( classifier );

        return fromMavenInformation ( mi );
    }

    private SortedMap<MetaKey, String> fromMavenInformation ( final MavenInformation info )
    {
        if ( info == null )
        {
            return Collections.emptySortedMap ();
        }

        try
        {
            return new TreeMap<> ( MetaKeys.unbind ( info ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }
}
