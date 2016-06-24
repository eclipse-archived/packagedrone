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
package org.eclipse.packagedrone.addon.internal;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.eclipse.packagedrone.utils.Suppressed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Watcher implements Closeable
{
    public static enum Event
    {
        ADDED,
        MODIFIED,
        REMOVED;
    }

    @FunctionalInterface
    public interface Listener
    {
        public void event ( Path path, Event event );
    }

    private final static Logger logger = LoggerFactory.getLogger ( Watcher.class );

    private final Path path;

    private final Listener listener;

    private final Thread thread;

    public Watcher ( final Path path, final Listener listener ) throws IOException
    {
        this.path = path;
        this.listener = listener;
        this.thread = new Thread ( this::run, "AddonManager/Watcher" );
        this.thread.start ();
    }

    private void run ()
    {
        try
        {
            while ( true )
            {
                try
                {
                    process ();
                    return;
                }
                catch ( final IOException e )
                {
                    logger.warn ( "Watcher failed", e );
                    try
                    {
                        Thread.sleep ( 10_000 );
                    }
                    catch ( final InterruptedException e1 )
                    {
                        return; // exit thread
                    }
                }
            }
        }
        finally
        {
            logger.warn ( "Watcher stopped watching: {}", this.path );
        }
    }

    private void process () throws IOException
    {
        logger.warn ( "Start watching: {}", this.path );

        try ( final WatchService watcher = this.path.getFileSystem ().newWatchService () )
        {
            this.path.register ( watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE );

            processExisting ( this.path );

            for ( ;; )
            {
                WatchKey key;
                try
                {
                    key = watcher.take ();
                }
                catch ( final InterruptedException e )
                {
                    return;
                }

                for ( final WatchEvent<?> event : key.pollEvents () )
                {
                    logger.debug ( "Handle event: {} - {}", event.kind (), event.context () );
                    handleEvent ( event );
                }

                if ( !key.reset () )
                {
                    throw new RuntimeException ( "Key got cancelled" );
                }
            }
        }
    }

    private void processExisting ( final Path path ) throws IOException
    {
        Files.list ( path ).forEachOrdered ( file -> {
            this.listener.event ( file, Event.ADDED );
        } );
    }

    @SuppressWarnings ( "unchecked" )
    private void handleEvent ( final WatchEvent<?> event )
    {
        final Kind<?> kind = event.kind ();

        if ( kind == StandardWatchEventKinds.OVERFLOW )
        {
            // FIXME: full rescan
            return;
        }

        final Path path = this.path.resolve ( ( (WatchEvent<Path>)event ).context () );

        if ( kind == StandardWatchEventKinds.ENTRY_CREATE )
        {
            this.listener.event ( path, Event.ADDED );
        }
        else if ( kind == StandardWatchEventKinds.ENTRY_DELETE )
        {
            this.listener.event ( path, Event.REMOVED );
        }
        else if ( kind == StandardWatchEventKinds.ENTRY_MODIFY )
        {
            this.listener.event ( path, Event.MODIFIED );
        }
    }

    @Override
    public void close () throws IOException
    {
        try ( Suppressed<RuntimeException> s = new Suppressed<> ( "Failed to dispose addon manager", RuntimeException::new ) )
        {
            if ( this.thread != null )
            {
                this.thread.interrupt ();
                s.run ( this.thread::join );
            }
        }
    }
}
