/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel;

import static org.eclipse.packagedrone.repo.channel.search.Predicates.and;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.equal;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.attribute;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.isNotNull;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.isNull;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.not;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.search.Predicate;
import org.eclipse.packagedrone.repo.channel.search.Predicates;
import org.eclipse.packagedrone.repo.channel.search.stream.StreamArtifactLocator;
import org.junit.Assert;
import org.junit.Test;

public class StreamArtifactLocatorTest
{
    private static final MetaKey KEY_FOO_VERSION = new MetaKey ( "foo", "version" );

    private static final MetaKey KEY_FOO_BAR = new MetaKey ( "foo", "bar" );

    private static final MetaKey KEY_SOME_OTHER = new MetaKey ( "some", "other" );

    private final ArrayList<ArtifactInformation> artifacts;

    public StreamArtifactLocatorTest ()
    {
        this.artifacts = new ArrayList<> ();

        this.artifacts.add ( makeMock ( "a1", "name1", "foo:bar", "baz", "foo:version", "1" ) );
        this.artifacts.add ( makeMock ( "a2", "name1", "foo:bar", "baz", "foo:version", "2" ) );
        this.artifacts.add ( makeMock ( "b1", "name2", "foo:bar", "buz", "foo:version", "1", "some:other", "value" ) );
        this.artifacts.add ( makeMock ( "b2", "name2", "foo:bar", "buz", "foo:version", "2" ) );
    }

    private ArtifactInformation makeMock ( final String id, final String name, final String... strings )
    {
        final Map<MetaKey, String> metaData = strings.length > 0 ? new HashMap<> ( strings.length / 2 ) : Collections.emptyMap ();

        for ( int i = 0; i < strings.length / 2; i++ )
        {
            final MetaKey key = MetaKey.fromString ( strings[i * 2] );
            final String value = strings[i * 2 + 1];
            metaData.put ( key, value );
        }

        return new ArtifactInformation ( id, null, null, name, 0L, Instant.now (), Collections.emptySet (), Collections.emptyList (), Collections.emptyMap (), metaData, null );
    }

    @Test
    public void testNull ()
    {
        final StreamArtifactLocator sal = new StreamArtifactLocator ( this.artifacts::stream );
        final List<ArtifactInformation> result = sal.search ( null );

        // expect same a input
        Assert.assertEquals ( this.artifacts, result );

        // clear result
        result.clear ();

        // original set must not be changed
        Assert.assertFalse ( this.artifacts.isEmpty () );
    }

    @Test
    public void testSearch1 ()
    {
        search ( Predicates.equal ( KEY_FOO_BAR, "baz" ), "a1", "a2" );
    }

    @Test
    public void testSearch2 ()
    {
        search ( Predicates.equal ( KEY_FOO_BAR, "buz" ), "b1", "b2" );
    }

    @Test
    public void testSearch3 ()
    {
        search ( Predicates.equal ( KEY_FOO_VERSION, "1" ), "a1", "b1" );
    }

    @Test
    public void testSearch4 ()
    {
        search ( Predicates.equal ( KEY_FOO_VERSION, "2" ), "a2", "b2" );
    }

    @Test
    public void testSearch5 ()
    {
        search ( and ( equal ( KEY_FOO_BAR, "buz" ), equal ( KEY_FOO_VERSION, "1" ) ), "b1" );
    }

    @Test
    public void testSearch6 ()
    {
        search ( and ( not ( equal ( KEY_FOO_BAR, "buz" ) ), equal ( KEY_FOO_VERSION, "1" ) ), "a1" );
    }

    @Test
    public void testSearch7 ()
    {
        search ( isNull ( attribute ( KEY_SOME_OTHER ) ), "a1", "a2", "b2" );
    }

    @Test
    public void testSearch8 ()
    {
        search ( isNotNull ( attribute ( KEY_SOME_OTHER ) ), "b1" );
    }

    private void search ( final Predicate predicate, final String... ids )
    {
        final StreamArtifactLocator sal = new StreamArtifactLocator ( this.artifacts::stream );
        final List<ArtifactInformation> result = sal.search ( predicate );

        final TreeSet<String> expected = new TreeSet<> ( Arrays.asList ( ids ) );
        final TreeSet<String> actual = result.stream ().map ( ArtifactInformation::getId ).collect ( Collectors.toCollection ( TreeSet::new ) );

        Assert.assertEquals ( expected, actual );
    }
}
