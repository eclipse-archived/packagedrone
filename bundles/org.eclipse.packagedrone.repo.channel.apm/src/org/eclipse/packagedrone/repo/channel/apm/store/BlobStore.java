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
package org.eclipse.packagedrone.repo.channel.apm.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.utils.IOConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingOutputStream;

public class BlobStore implements Closeable
{
    private final static Logger logger = LoggerFactory.getLogger ( BlobStore.TransactionImpl.class );

    public class TransactionImpl implements Transaction
    {
        private boolean done;

        private final Set<String> deleted = new HashSet<> ();

        private final Set<String> added = new HashSet<> ();

        @Override
        public boolean delete ( final String id ) throws IOException
        {
            testDone ();

            final boolean existed = BlobStore.this.currentIndex.contains ( id ) || this.added.contains ( id );

            this.added.remove ( id );
            this.deleted.add ( id );

            return existed;
        }

        @Override
        public long create ( final String id, final IOConsumer<OutputStream> consumer ) throws IOException
        {
            testDone ();

            final long size = handleCreate ( id, consumer );

            this.added.add ( id );
            this.deleted.remove ( id );

            return size;
        }

        @Override
        public boolean stream ( final String id, final IOConsumer<InputStream> consumer ) throws IOException
        {
            testDone ();

            if ( this.deleted.contains ( id ) )
            {
                return false;
            }

            return handleStream ( id, consumer );
        }

        @Override
        public void commit ()
        {
            testDone ();
            this.done = true;

            handleCommit ( this, this.deleted, this.added );
        }

        @Override
        public void rollback ()
        {
            testDone ();
            this.done = true;

            handleRollback ( this );
        }

        protected void testDone ()
        {
            if ( this.done )
            {
                throw new IllegalStateException ( "Transaction already closed" );
            }
        }
    }

    public static interface Transaction
    {
        public boolean delete ( String id ) throws IOException;

        public default long create ( final String id, final InputStream source ) throws IOException
        {
            return create ( id, target -> ByteStreams.copy ( source, target ) );
        }

        public long create ( String id, IOConsumer<OutputStream> consumer ) throws IOException;

        public boolean stream ( String id, IOConsumer<InputStream> consumer ) throws IOException;

        public void commit ();

        public void rollback ();
    }

    private final Path base;

    private final Path dataPath;

    private Transaction transaction;

    private Set<String> currentIndex;

    private final Path indexPath;

    public BlobStore ( final Path path ) throws IOException
    {
        this.base = path;

        this.dataPath = this.base.resolve ( "data" ).toAbsolutePath ();
        this.indexPath = this.base.resolve ( "index.txt" ).toAbsolutePath ();

        Files.createDirectories ( this.dataPath );

        Set<String> index = loadIndex ();
        if ( index == null )
        {
            // build index
            index = scanIndex ( path );
            writeIndex ( index );
        }
        this.currentIndex = index;
    }

    public static Set<String> scanIndex ( final Path basePath ) throws IOException
    {
        try
        {
            return Files.walk ( basePath.resolve ( "data" ) ).filter ( Files::isRegularFile ).map ( path -> path.getName ( path.getNameCount () - 1 ).toString () ).collect ( Collectors.toSet () );
        }
        catch ( final NoSuchFileException e )
        {
            return Collections.emptySet ();
        }
    }

    public boolean handleStream ( final String id, final IOConsumer<InputStream> consumer ) throws IOException
    {
        return processStream ( id, consumer );
    }

    public long handleCreate ( final String id, final IOConsumer<OutputStream> consumer ) throws IOException
    {
        final Path path = makeDataPath ( id );

        Files.createDirectories ( path.getParent () );

        try ( CountingOutputStream stream = new CountingOutputStream ( new BufferedOutputStream ( Files.newOutputStream ( path, StandardOpenOption.CREATE_NEW ) ) ) )
        {
            consumer.accept ( stream );
            return stream.getCount ();
        }
    }

    @Override
    public synchronized void close ()
    {
        if ( this.transaction != null )
        {
            handleRollback ( this.transaction );
        }
    }

    public boolean stream ( final String id, final IOConsumer<InputStream> consumer ) throws IOException
    {
        if ( !this.currentIndex.contains ( id ) )
        {
            // FIXME: check locking
            return false;
        }
        return processStream ( id, consumer );
    }

    private boolean processStream ( final String id, final IOConsumer<InputStream> consumer ) throws IOException
    {
        final Path path = makeDataPath ( id );

        try ( InputStream stream = new BufferedInputStream ( Files.newInputStream ( path, StandardOpenOption.READ ) ) )
        {
            consumer.accept ( stream );
            return true;
        }
        catch ( final NoSuchFileException e )
        {
            return false;
        }
    }

    public synchronized Transaction start ()
    {
        if ( this.transaction == null )
        {
            this.transaction = new TransactionImpl ();
            return this.transaction;
        }

        throw new IllegalStateException ( "Transaction already in progress" );
    }

    protected synchronized void handleCommit ( final Transaction transaction, final Set<String> deleted, final Set<String> added )
    {
        if ( this.transaction != transaction )
        {
            throw new IllegalStateException ( "Invalid transaction" );
        }
        this.transaction = null;

        try
        {
            final Set<String> index = new HashSet<> ( this.currentIndex );
            index.removeAll ( deleted );
            index.addAll ( added );

            writeIndex ( index );

            for ( final String id : deleted )
            {
                Files.deleteIfExists ( makeDataPath ( id ) );
            }
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to commit", e );
        }
    }

    private void writeIndex ( final Set<String> index ) throws IOException
    {
        Files.write ( this.indexPath, index, StandardCharsets.UTF_8 );
        this.currentIndex = index;
    }

    private Set<String> loadIndex () throws IOException
    {
        try
        {
            return new HashSet<> ( Files.readAllLines ( this.indexPath, StandardCharsets.UTF_8 ) );
        }
        catch ( final NoSuchFileException e )
        {
            return null;
        }
    }

    protected synchronized void handleRollback ( final Transaction transaction )
    {
        if ( this.transaction != transaction )
        {
            throw new IllegalStateException ( "Invalid transaction" );
        }
        this.transaction = null;

        try
        {
            vacuum ();
        }
        catch ( final IOException e )
        {
            logger.warn ( "Failed to vacuum", e );
        }
    }

    public synchronized void vacuum () throws IOException
    {
        final Set<String> ids = scanIndex ( this.base );
        ids.removeAll ( this.currentIndex );

        for ( final String id : ids )
        {
            final Path path = makeDataPath ( id );
            logger.debug ( "Vacuuming file: {}", path );
            Files.deleteIfExists ( path );
        }
    }

    private Path makeDataPath ( final String id )
    {
        final String l1 = id.substring ( 0, 1 );
        final String l2 = id.substring ( 1, 2 );
        return this.dataPath.resolve ( Paths.get ( l1, l2, id ) );
    }

}
