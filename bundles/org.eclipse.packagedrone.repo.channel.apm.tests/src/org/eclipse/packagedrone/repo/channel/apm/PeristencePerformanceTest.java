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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore;
import org.junit.Assert;
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

        assertModels ( ctx, read );

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

    private void assertModels ( final ModifyContextImpl ctx, final ModifyContextImpl read )
    {
        assertValue ( ctx, read, c -> c.getState ().getDescription () );
        assertValue ( ctx, read, c -> c.getState ().isLocked () );
        assertValue ( ctx, read, c -> c.getState ().getCreationTimestamp () );
        assertValue ( ctx, read, c -> c.getState ().getModificationTimestamp () );
        assertValue ( ctx, read, c -> c.getState ().getNumberOfArtifacts () );
        assertValue ( ctx, read, c -> c.getState ().getNumberOfBytes () );

        assertCollection ( ctx, read, c -> c.getAspectStates ().keySet (), Assert::assertEquals );
        assertMap ( ctx, read, c -> c.getExtractedMetaData (), Assert::assertEquals );
        assertMap ( ctx, read, c -> c.getProvidedMetaData (), Assert::assertEquals );

        assertValidationMessages ( ctx.getValidationMessages (), read.getValidationMessages () );

        assertMap ( ctx, read, ModifyContextImpl::getCacheEntries, ( a, b ) -> {
            assertValue ( a, b, CacheEntryInformation::getKey );
            assertValue ( a, b, CacheEntryInformation::getMimeType );
            assertValue ( a, b, CacheEntryInformation::getName );
            assertValue ( a, b, CacheEntryInformation::getSize );
            assertValue ( a, b, CacheEntryInformation::getTimestamp );
        } );

        assertArtifacts ( ctx.getArtifacts (), read.getArtifacts () );
    }

    private void assertValidationMessages ( final Collection<ValidationMessage> v1, final Collection<ValidationMessage> v2 )
    {
        assertCollection ( v1, v2, v -> v, ( a, b ) -> {
            assertValue ( a, b, ValidationMessage::getSeverity );
            assertValue ( a, b, ValidationMessage::getMessage );
            assertValue ( a, b, ValidationMessage::getAspectId );
            assertCollection ( a, b, ValidationMessage::getArtifactIds, Assert::assertEquals );
        } );
    }

    private void assertArtifacts ( final Map<String, ArtifactInformation> a1, final Map<String, ArtifactInformation> a2 )
    {
        assertMap ( a1, a2, a -> a, ( a, b ) -> {
            assertValue ( a, b, ArtifactInformation::getId );
            assertValue ( a, b, ArtifactInformation::getCreationInstant );
            assertValue ( a, b, ArtifactInformation::getName );
            assertValue ( a, b, ArtifactInformation::getSize );

            assertValue ( a, b, ArtifactInformation::getParentId );
            assertCollection ( a, b, ArtifactInformation::getChildIds, Assert::assertEquals );

            assertCollection ( a, b, ArtifactInformation::getFacets, Assert::assertEquals );

            assertMap ( a, b, c -> c.getExtractedMetaData (), Assert::assertEquals );
            assertMap ( a, b, c -> c.getProvidedMetaData (), Assert::assertEquals );

            assertValidationMessages ( a.getValidationMessages (), b.getValidationMessages () );

            assertValue ( a, b, ArtifactInformation::getVirtualizerAspectId );
        } );
    }

    private static <C, T> void assertMap ( final C ctx1, final C ctx2, final Function<C, Map<?, T>> func, final BiConsumer<T, T> comparator )
    {
        final Map<?, T> col1 = func.apply ( ctx1 );
        final Map<?, T> col2 = func.apply ( ctx2 );

        Assert.assertNotNull ( col1 );
        Assert.assertNotNull ( col2 );

        Assert.assertArrayEquals ( col1.keySet ().toArray (), col2.keySet ().toArray () );
        for ( final Map.Entry<?, T> entry : col1.entrySet () )
        {
            final T value2 = col2.get ( entry.getKey () );
            comparator.accept ( entry.getValue (), value2 );
        }
    }

    @SuppressWarnings ( "unchecked" )
    private static <C, T> void assertCollection ( final C ctx1, final C ctx2, final Function<C, Collection<T>> func, final BiConsumer<T, T> comparator )
    {
        final Collection<?> col1 = func.apply ( ctx1 );
        final Collection<?> col2 = func.apply ( ctx2 );

        Assert.assertNotNull ( col1 );
        Assert.assertNotNull ( col2 );

        // test size first

        Assert.assertEquals ( col1.size (), col2.size () );

        // convert to arrays ... we want position access, fast

        final Object[] a1 = col1.toArray ();
        final Object[] a2 = col2.toArray ();

        // both arrays will have the same size

        for ( int i = 0; i < a1.length; i++ )
        {
            comparator.accept ( (T)a1[i], (T)a2[i] );
        }

        Assert.assertArrayEquals ( col1.toArray (), col2.toArray () );
    }

    private static <T, R> void assertValue ( final T ctx1, final T ctx2, final Function<T, R> func )
    {
        Assert.assertEquals ( func.apply ( ctx1 ), func.apply ( ctx2 ) );
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
        state.setDescription ( "Foo Bar" );
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
