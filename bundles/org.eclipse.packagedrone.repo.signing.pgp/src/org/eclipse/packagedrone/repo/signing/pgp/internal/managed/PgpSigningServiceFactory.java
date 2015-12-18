/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.signing.pgp.internal.managed;

import static org.eclipse.packagedrone.utils.Locks.lock;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.signing.pgp.ManagedPgpConfiguration;
import org.eclipse.packagedrone.repo.signing.pgp.ManagedPgpFactory;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.util.ReplaceOnCloseWriter;
import org.eclipse.packagedrone.utils.Locks.Locked;
import org.eclipse.packagedrone.utils.Suppressed;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.google.gson.GsonBuilder;

public class PgpSigningServiceFactory implements ManagedPgpFactory
{
    private StorageManager manager;

    private Path base;

    private Map<String, Entry> entries = new HashMap<> ();

    private final Lock readLock;

    private final Lock writeLock;

    private final BundleContext context;

    public void setManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public PgpSigningServiceFactory ()
    {
        this.context = FrameworkUtil.getBundle ( PgpSigningServiceFactory.class ).getBundleContext ();

        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock ();
        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();
    }

    public void start ()
    {
        this.base = this.manager.getContext ().getBasePath ();
        Map<String, Entry> entries;
        try ( final Locked l = lock ( this.writeLock ) )
        {
            entries = load ();
            this.entries = new HashMap<> ( entries );
        }

        // activate all outside the lock

        entries.values ().forEach ( Entry::start );
    }

    public void stop ()
    {
        // dispose all

        List<Entry> entries;
        try ( final Locked l = lock ( this.writeLock ) )
        {

            entries = new ArrayList<> ( this.entries.values () );
            entries.clear ();
        }

        try ( final Suppressed<RuntimeException> s = new Suppressed<> ( "Failed to stop", RuntimeException::new ) )
        {
            for ( final Entry entry : entries )
            {
                s.run ( entry::stop );
            }
        }
    }

    @Override
    public ManagedPgpConfiguration createService ( final String label, final String key, final String passphrase )
    {
        final Entry old;
        final Entry entry;

        try ( final Locked l = lock ( this.writeLock ) )
        {
            final String id = UUID.randomUUID ().toString ();

            final Configuration cfg = new Configuration ();
            cfg.setId ( id );
            cfg.setLabel ( label );
            cfg.setSecretKey ( key );
            cfg.setPassphrase ( passphrase );

            entry = new Entry ( this.context, cfg );

            old = this.entries.put ( id, entry );

            try
            {
                store ();
            }
            catch ( final Exception e )
            {
                // restore old state

                this.entries.remove ( id );
                if ( old != null )
                {
                    this.entries.put ( id, old );
                }
                throw new RuntimeException ( e );
            }

            if ( old != null )
            {
                old.stop ();
            }
        }

        // activate the entry outside the lock

        if ( old != null )
        {
            old.stop ();
        }

        entry.start ();

        return entry.getConfiguration ();
    }

    private Path getFile ()
    {
        return this.base.resolve ( "pgp-keys.json" );
    }

    private void store () throws IOException
    {
        try ( ReplaceOnCloseWriter writer = new ReplaceOnCloseWriter ( getFile (), StandardCharsets.UTF_8 ) )
        {
            final Object[] cfgs = this.entries.values ().stream ().map ( Entry::getRawConfiguration ).toArray ();
            new GsonBuilder ().create ().toJson ( cfgs, writer );
            writer.commit ();
        }
    }

    private Map<String, Entry> load ()
    {
        Map<String, Entry> result = null;

        try ( Reader reader = Files.newBufferedReader ( getFile () ) )
        {
            final Configuration[] cfgs = new GsonBuilder ().create ().fromJson ( reader, Configuration[].class );
            result = new HashMap<> ( cfgs.length );
            for ( final Configuration cfg : cfgs )
            {
                final Entry entry = new Entry ( this.context, cfg );
                result.put ( entry.getRawConfiguration ().getId (), entry );
            }
        }
        catch ( final IOException e )
        {
            // ignore
        }

        return result != null ? result : new HashMap<> ();
    }

    @Override
    public void deleteService ( final String id )
    {
        Entry entry = null;
        try ( final Locked l = lock ( this.writeLock ) )
        {
            entry = this.entries.remove ( id );

            try
            {
                store ();
            }
            catch ( final Exception e )
            {
                // restore old state

                if ( entry != null )
                {
                    this.entries.put ( id, entry );
                    entry = null;
                }
                throw new RuntimeException ( e );
            }
        }
        finally
        {
            if ( entry != null )
            {
                // stop outside the lock

                entry.stop ();
            }
        }
    }

    @Override
    public List<ManagedPgpConfiguration> list ( final int start, final int amount )
    {
        try ( final Locked l = lock ( this.readLock ) )
        {
            return this.entries.entrySet ().stream ().map ( entry -> entry.getValue ().getConfiguration () ).collect ( Collectors.toList () );
        }
    }
}
