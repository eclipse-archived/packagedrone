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
package org.eclipse.packagedrone.repo.channel.apm.blob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore.Transaction;
import org.eclipse.packagedrone.repo.utils.Holder;
import org.eclipse.packagedrone.repo.utils.IOConsumer;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.CharStreams;

public class CacheStoreTest
{
    private static Path basePath;

    @BeforeClass
    public static void setup () throws IOException
    {
        basePath = Paths.get ( "tests" );

        if ( Files.exists ( basePath ) )
        {
            Files.walkFileTree ( basePath, new RecursiveDeleteVisitor () );
        }

        Files.createDirectories ( basePath );
    }

    @Test
    public void test1 () throws IOException
    {
        try ( CacheStore store = new CacheStore ( basePath.resolve ( "test1" ) ) )
        {
            final boolean result = store.stream ( new MetaKey ( "a", "b" ), stream -> {
            } );

            assertFalse ( result );
        }
    }

    /**
     * Empty commit
     */
    @Test
    public void test2a () throws IOException
    {
        try ( CacheStore store = new CacheStore ( basePath.resolve ( "test2a" ) ) )
        {
            final Transaction t = store.startTransaction ();
            t.commit ();
        }
    }

    /**
     * Empty rollback
     */
    @Test
    public void test2b () throws IOException
    {
        try ( CacheStore store = new CacheStore ( basePath.resolve ( "test2b" ) ) )
        {
            final Transaction t = store.startTransaction ();
            t.rollback ();
        }
    }

    /**
     * Double start
     */
    @Test ( expected = Throwable.class )
    public void test3a () throws IOException
    {
        try ( CacheStore store = new CacheStore ( basePath.resolve ( "test3a" ) ) )
        {
            store.startTransaction ();
            store.startTransaction ();
        }
    }

    private static IOConsumer<OutputStream> text ( final String text )
    {
        return ( stream ) -> {
            try ( Writer writer = new OutputStreamWriter ( stream, StandardCharsets.UTF_8 ) )
            {
                writer.write ( text );
            }
        };
    }

    @FunctionalInterface
    interface Streamer
    {
        public boolean streamer ( final MetaKey key, final IOConsumer<InputStream> consumer ) throws IOException;
    }

    private static String streamed ( final MetaKey key, final Streamer func ) throws IOException
    {
        final Holder<String> result = new Holder<> ();

        final boolean found = func.streamer ( key, ( stream ) -> {
            final InputStreamReader reader = new InputStreamReader ( stream, StandardCharsets.UTF_8 );
            result.value = CharStreams.toString ( reader );
        } );

        if ( !found )
        {
            return null;
        }

        return result.value;
    }

    /**
     * Store
     */
    @Test
    public void test4a () throws IOException
    {
        final MetaKey key1 = new MetaKey ( "foo", "bar1" );

        try ( CacheStore store = new CacheStore ( basePath.resolve ( "test4a" ) ) )
        {
            Transaction t = store.startTransaction ();
            t.put ( key1, text ( "Foo Bar" ) );
            t.rollback ();

            assertFalse ( store.stream ( key1, stream -> {
            } ) );

            t = store.startTransaction ();
            t.put ( key1, text ( "Foo Bar 2" ) );

            assertNull ( streamed ( key1, store::stream ) ); // still uncommitted
            assertEquals ( "Foo Bar 2", streamed ( key1, t::stream ) ); // in transaction

            t.commit ();

            assertEquals ( "Foo Bar 2", streamed ( key1, store::stream ) ); // still uncommitted
        }
    }

    /**
     * Store
     */
    @Test
    public void test4b () throws IOException
    {
        final MetaKey key1 = new MetaKey ( "foo", "bar1" );

        try ( CacheStore store = new CacheStore ( basePath.resolve ( "test4b" ) ) )
        {
            Transaction t = store.startTransaction ();
            t.put ( key1, text ( "Foo Bar" ) );
            t.rollback ();

            assertFalse ( store.stream ( key1, stream -> {
            } ) );

            t = store.startTransaction ();
            t.put ( key1, text ( "Foo Bar 2" ) );

            assertNull ( streamed ( key1, store::stream ) ); // still uncommitted
            assertEquals ( "Foo Bar 2", streamed ( key1, t::stream ) ); // in transaction

            t.rollback ();

            assertNull ( streamed ( key1, store::stream ) ); // still uncommitted
        }
    }

    /**
     * Store
     */
    @Test
    public void test4c () throws IOException
    {
        final MetaKey key1 = new MetaKey ( "foo", "bar1" );

        try ( CacheStore store = new CacheStore ( basePath.resolve ( "test4c" ) ) )
        {
            Transaction t = store.startTransaction ();
            t.put ( key1, text ( "Foo Bar" ) );
            t.rollback ();

            assertFalse ( store.stream ( key1, stream -> {
            } ) );

            t = store.startTransaction ();
            t.put ( key1, text ( "Foo Bar 2" ) );

            assertNull ( streamed ( key1, store::stream ) ); // still uncommitted
            assertEquals ( "Foo Bar 2", streamed ( key1, t::stream ) ); // in transaction

            t.commit ();

            assertEquals ( "Foo Bar 2", streamed ( key1, store::stream ) ); // still uncommitted

            t = store.startTransaction ();

            t.put ( key1, text ( "Foo Bar 3" ) );
            assertEquals ( "Foo Bar 3", streamed ( key1, t::stream ) ); // in transaction
            t.rollback ();

            assertEquals ( "Foo Bar 2", streamed ( key1, store::stream ) ); // in transaction
        }
    }
}
