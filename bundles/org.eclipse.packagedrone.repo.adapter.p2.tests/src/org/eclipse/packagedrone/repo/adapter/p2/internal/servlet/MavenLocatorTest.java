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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.junit.BeforeClass;
import org.junit.Test;

public class MavenLocatorTest
{
    private @NonNull static final List<ArtifactInformation> arts = new LinkedList<> ();

    @BeforeClass
    public static void setup ()
    {
        arts.add ( makeMock ( "id1", "g1", "a1", "v1", null, null, null ) );
        arts.add ( makeMock ( "id2", "g1", "a1", "v2", null, null, null ) );
        arts.add ( makeMock ( "id3", "g1", "a1", "v3", null, null, null ) );
        arts.add ( makeMock ( "id4", "g1", "a1", "v3-SNAPSHOT", "v3.1.2.3", null, null ) );
        arts.add ( makeMock ( "id5", "g1", "a1", "v3-SNAPSHOT", "v3.1.2.3", "ext1", "class1" ) );
        arts.add ( makeMock ( "id6", "g1", "a1", "v3-SNAPSHOT", "v3.1.2.3", "ext1", "class2" ) );
        arts.add ( makeMock ( "id7", "g1", "a1", "v3-SNAPSHOT", "v3.1.2.3", "ext2", "class1" ) );
        arts.add ( makeMock ( "id8", "g1", "a1", "v3-SNAPSHOT", "v3.1.2.3", "ext2", "class2" ) );

        arts.add ( makeMock ( "id10", "g1", "a1", "v4", null, null, null ) );
        arts.add ( makeMock ( "id20", "g1", "a1", "v4", null, null, null ) );
        arts.add ( makeMock ( "id30", "g1", "a1", "v4", null, null, null ) );
    }

    @Test
    public void test1 ()
    {
        final List<ArtifactInformation> r = MavenLocator.findByMavenCoordinates ( arts, null, null, null, null, null, null );
        assertTrue ( r.isEmpty () );
    }

    @Test
    public void test2 ()
    {
        final List<ArtifactInformation> r = MavenLocator.findByMavenCoordinates ( arts, "g1", "a1", "v1", null, null, null );
        assertEquals ( r.size (), 1 );
        assertEquals ( r.get ( 0 ).getId (), "id1" );
    }

    @Test
    public void test3a ()
    {
        final List<ArtifactInformation> r = MavenLocator.findByMavenCoordinates ( arts, "g1", "a1", "v3", null, null, null );
        assertEquals ( r.size (), 1 );
        assertEquals ( r.get ( 0 ).getId (), "id3" );
    }

    @Test
    public void test3b ()
    {
        final List<ArtifactInformation> r = MavenLocator.findByMavenCoordinates ( arts, "g1", "a1", "v3-SNAPSHOT", null, null, null );
        assertTrue ( r.isEmpty () );
    }

    @Test
    public void test3c ()
    {
        final List<ArtifactInformation> r = MavenLocator.findByMavenCoordinates ( arts, "g1", "a1", "v3-SNAPSHOT", "v3.1.2.3", null, null );
        assertEquals ( r.size (), 1 );
        assertEquals ( r.get ( 0 ).getId (), "id4" );
    }

    @Test
    public void test3d ()
    {
        final List<ArtifactInformation> r = MavenLocator.findByMavenCoordinates ( arts, "g1", "a1", "v3-SNAPSHOT", "v3.1.2.3", "ext2", "class1" );
        assertEquals ( r.size (), 1 );
        assertEquals ( r.get ( 0 ).getId (), "id7" );
    }

    @Test
    public void test4a ()
    {
        final List<ArtifactInformation> r = MavenLocator.findByMavenCoordinates ( arts, "g1", "a1", "v4", null, null, null );
        assertEquals ( r.size (), 3 );
        assertEquals ( r.get ( 0 ).getId (), "id10" );
        assertEquals ( r.get ( 1 ).getId (), "id20" );
        assertEquals ( r.get ( 2 ).getId (), "id30" );
    }

    private static ArtifactInformation makeMock ( final String id, final String groupId, final String artifactId, final String version, final String snapshotVersion, final String extension, final String classifier )
    {
        final Map<MetaKey, String> md = new HashMap<> ();

        if ( groupId != null )
        {
            md.put ( MavenLocator.KEY_GROUP_ID, groupId );
        }
        if ( artifactId != null )
        {
            md.put ( MavenLocator.KEY_ARTIFACT_ID, artifactId );
        }
        if ( version != null )
        {
            md.put ( MavenLocator.KEY_VERSION, version );
        }
        if ( snapshotVersion != null )
        {
            md.put ( MavenLocator.KEY_SNAPSHOT_VERSION, snapshotVersion );
        }
        if ( extension != null )
        {
            md.put ( MavenLocator.KEY_EXTENSION, extension );
        }
        if ( classifier != null )
        {
            md.put ( MavenLocator.KEY_CLASSIFIER, classifier );
        }

        return new ArtifactInformation ( id, null, null, id, 0L, Instant.now (), emptySet (), emptyList (), emptyMap (), md, null );
    }
}
