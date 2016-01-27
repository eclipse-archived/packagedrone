/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.apm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersistenceTest
{
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
    public void testEmptyDescription () throws IOException
    {
        final Map<String, ArtifactInformation> artifacts = new HashMap<> ();

        final Map<String, String> aspects = new HashMap<> ();

        final ChannelState.Builder state = new ChannelState.Builder ();

        state.setCreationTimestamp ( Instant.now () );
        state.setModificationTimestamp ( Instant.now () );
        state.setLocked ( false );
        state.setNumberOfArtifacts ( 0 );
        state.setNumberOfBytes ( 0L );

        final Map<MetaKey, String> extractedMetaData = new HashMap<> ();
        final Map<MetaKey, String> providedMetaData = new HashMap<> ();

        final Map<MetaKey, CacheEntryInformation> cacheEntries = new HashMap<> ();

        final ModifyContextImpl model = new ModifyContextImpl ( "id", store, cacheStore, state.build (), aspects, artifacts, cacheEntries, extractedMetaData, providedMetaData );

        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        try ( ChannelWriter w = new ChannelWriter ( bos ) )
        {
            w.write ( model );
        }
        bos.close ();

        ModifyContextImpl model2;
        try ( final ChannelReader r = new ChannelReader ( new ByteArrayInputStream ( bos.toByteArray () ), "id", store, cacheStore ) )
        {
            model2 = r.read ();
        }

        ModelAssert.assertModels ( model, model2 );
    }
}
