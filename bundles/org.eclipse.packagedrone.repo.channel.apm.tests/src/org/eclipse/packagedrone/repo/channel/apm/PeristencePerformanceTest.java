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
package org.eclipse.packagedrone.repo.channel.apm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore;
import org.junit.BeforeClass;
import org.junit.Test;

public class PeristencePerformanceTest
{
    private static final MetaKey KEY_BAR_FOO = new MetaKey ( "bar", "foo" );

    private static final MetaKey KEY_BAR_FOO_TOO = new MetaKey ( "bar", "foo-too" );

    private static Path root;

    private static BlobStore store;

    private static CacheStore cacheStore;

    @BeforeClass
    public static void setup () throws IOException
    {
        root = Paths.get ( "tests", "channel" );
        Files.createDirectories ( root );

        store = new BlobStore ( root.resolve ( "blobs" ) );
        cacheStore = new CacheStore ( root.resolve ( "cache" ) );
    }

    @Test
    public void test1 () throws IOException
    {
        Instant start = Instant.now ();

        final ModifyContextImpl ctx = makeModel ();

        start = tick ( start, "Create model" );

        waitFor ();

        try ( OutputStream stream = new BufferedOutputStream ( Files.newOutputStream ( root.resolve ( "state.json" ) ) );
              ChannelWriter writer = new ChannelWriter ( stream ); )
        {
            writer.write ( ctx );
        }

        start = tick ( start, "Write model (default)" );

        waitFor ();

        final ModifyContextImpl read;
        try ( InputStream stream = new BufferedInputStream ( Files.newInputStream ( root.resolve ( "state.json" ) ) );
              ChannelReader reader = new ChannelReader ( stream, "id", null, store, cacheStore ); )
        {
            read = reader.read ();
        }

        start = tick ( start, "Read model (default)" );

        ModelAssert.assertModels ( ctx, read );

        start = tick ( start, "Validate model" );
    }

    private void waitFor () throws IOException
    {
        if ( Boolean.getBoolean ( "waitForKey" ) )
        {
            System.out.println ( "Waiting for key..." );
            System.out.flush ();
            System.in.read ();
        }
    }

    private Instant tick ( final Instant start, final String job )
    {
        final Instant n = Instant.now ();
        final Duration diff = Duration.between ( start, n );
        System.out.format ( "%s ms - %s%n", diff.toMillis (), job );
        return n;
    }

    private ModifyContextImpl makeModel ()
    {
        // artifacts

        final int numberOfArts = 100_000;

        final Map<String, ArtifactInformation> artifacts = new HashMap<> ( numberOfArts );

        for ( int i = 0; i < numberOfArts; i++ )
        {
            final String id = UUID.randomUUID ().toString ();

            final Map<MetaKey, String> extractedMetaData = new HashMap<> ();
            final Map<MetaKey, String> providedMetaData = new HashMap<> ();

            for ( int j = 0; j < 10; j++ )
            {
                extractedMetaData.put ( new MetaKey ( "foo", "bar_" + j ), makeData ( 200 ) );
            }

            providedMetaData.put ( KEY_BAR_FOO, "foobar" );

            final ArtifactInformation art = new ArtifactInformation ( id, null, Collections.emptySet (), "foo_bar_" + i, 123456, Instant.now (), Collections.singleton ( "foo" ), Collections.emptyList (), providedMetaData, extractedMetaData, "virtual1" );

            artifacts.put ( id, art );
        }

        // channel state

        final ChannelState.Builder state = new ChannelState.Builder ();

        state.setCreationTimestamp ( Instant.now () );
        state.setModificationTimestamp ( Instant.now () );
        state.setLocked ( false );
        state.setNumberOfArtifacts ( numberOfArts );
        state.setNumberOfBytes ( numberOfArts * 123456L );

        // aspect states

        final Map<String, String> aspects = new HashMap<> ();
        aspects.put ( "id1", "version" );
        aspects.put ( "id2", null );

        // channel meta data

        final Map<MetaKey, String> extractedMetaData = new HashMap<> ();
        extractedMetaData.put ( KEY_BAR_FOO, "foobar" );
        extractedMetaData.put ( KEY_BAR_FOO_TOO, "foobar2" );
        final Map<MetaKey, String> providedMetaData = new HashMap<> ();
        providedMetaData.put ( KEY_BAR_FOO, "foobar" );
        providedMetaData.put ( KEY_BAR_FOO_TOO, "foobar2" );

        // cache entries

        final Map<MetaKey, CacheEntryInformation> cacheEntries = new HashMap<> ();
        cacheEntries.put ( KEY_BAR_FOO, new CacheEntryInformation ( KEY_BAR_FOO, "name1", 123, "foo/bar", Instant.now () ) );
        cacheEntries.put ( KEY_BAR_FOO_TOO, new CacheEntryInformation ( KEY_BAR_FOO_TOO, "name1", 123, "foo/bar", Instant.now () ) );

        return new ModifyContextImpl ( "id", null, store, cacheStore, state.build (), aspects, artifacts, cacheEntries, extractedMetaData, providedMetaData );
    }

    private String makeData ( final int i )
    {
        final char[] c = new char[i];
        Arrays.fill ( c, 'a' );
        return String.valueOf ( c );
    }
}
