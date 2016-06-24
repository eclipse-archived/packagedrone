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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.addon.Addon;
import org.eclipse.packagedrone.addon.AddonManager;
import org.eclipse.packagedrone.addon.State;
import org.eclipse.packagedrone.addon.internal.Watcher.Event;
import org.eclipse.packagedrone.utils.Suppressed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddonManagerImpl implements AddonManager
{
    private static final String SUFFIX_ENABLE = ".enable";

    private static final String EXTENSION_ADDON = ".addon";

    private static final String EXTENSION_ADDON_ENABLED = EXTENSION_ADDON + SUFFIX_ENABLE;

    private final static Logger logger = LoggerFactory.getLogger ( AddonManagerImpl.class );

    private final Path path;

    private final Watcher watcher;

    private ExecutorService executor;

    private final Map<String, AddonRegistration> addons = new HashMap<> ();

    public AddonManagerImpl ( final Path path ) throws IOException
    {
        this.path = path;
        this.watcher = new Watcher ( path, this::handleEvent );
        this.executor = Executors.newSingleThreadExecutor ( new ThreadFactory () {

            @Override
            public Thread newThread ( final Runnable r )
            {
                return new Thread ( r, "AddonManager/Worker" );
            }
        } );
    }

    public void dispose ()
    {
        try ( Suppressed<RuntimeException> s = new Suppressed<> ( "Failed to shut down addon manager", RuntimeException::new ) )
        {
            s.close ( this.watcher );

            synchronized ( this )
            {
                s.run ( this.executor::shutdown );

                for ( final AddonRegistration reg : this.addons.values () )
                {
                    reg.stop ();
                }
                this.addons.clear ();
            }
        }
    }

    private void handleEvent ( final Path path, final Watcher.Event event )
    {
        logger.warn ( "Handle event: {} -> {}", event, path );

        if ( path.toString ().endsWith ( EXTENSION_ADDON ) )
        {
            handleAddon ( path, event );
        }
        else if ( path.toString ().endsWith ( EXTENSION_ADDON_ENABLED ) )
        {
            handleDisabled ( path, event );
        }
    }

    private void handleAddon ( final Path path, final Event event )
    {
        final String addonName = path.getFileName ().toString ();

        switch ( event )
        {
            case ADDED:
                this.executor.execute ( () -> internalAdd ( addonName ) );
                break;
            case REMOVED:
                this.executor.execute ( () -> internalRemove ( addonName ) );
                break;
            case MODIFIED:
                this.executor.execute ( () -> {
                    internalRemove ( addonName );
                    internalAdd ( addonName );
                } );
                break;
            default:
                // unknown event, simply ignore
                break;
        }
    }

    private static String removeSuffix ( final String string, final String suffix )
    {
        if ( string.endsWith ( suffix ) )
        {
            return string.substring ( 0, string.length () - suffix.length () );
        }
        else
        {
            return string;
        }
    }

    private void handleDisabled ( final Path path, final Event event )
    {
        final String addonName = removeSuffix ( path.getFileName ().toString (), SUFFIX_ENABLE );

        switch ( event )
        {
            case ADDED:
                this.executor.execute ( () -> internalEnable ( addonName ) );
                break;
            case REMOVED:
                this.executor.execute ( () -> internalDisable ( addonName ) );
                break;
            case MODIFIED:
                // simply ignore
                break;
            default:
                // unknown event, simply ignore
                break;
        }
    }

    private boolean isEnabled ( final String addonName )
    {
        return Files.exists ( this.path.resolve ( addonName + SUFFIX_ENABLE ) );
    }

    private synchronized void internalAdd ( final String addonName )
    {
        logger.warn ( "Add: {}", addonName );

        final AddonRegistration addon = new DefaultAddonRegistration ( this.path.resolve ( addonName ) );

        final AddonRegistration oldAddon = this.addons.put ( addonName, addon );
        if ( oldAddon != null )
        {
            oldAddon.stop ();
        }

        if ( isEnabled ( addonName ) )
        {
            logger.warn ( "Addon is initially enabled" );
            if ( addon.getInformation ().getState () == State.INSTALLED )
            {
                addon.start ();
            }
        }
    }

    private synchronized void internalRemove ( final String addonName )
    {
        logger.warn ( "Remove: {}", addonName );

        final AddonRegistration addon = this.addons.remove ( addonName );
        if ( addon != null )
        {
            addon.stop ();
        }
    }

    private synchronized void internalEnable ( final String addonName )
    {
        logger.warn ( "Enable addon: {}", addonName );

        final AddonRegistration addon = this.addons.get ( addonName );
        if ( addon != null )
        {
            addon.start ();
        }
    }

    private synchronized void internalDisable ( final String addonName )
    {
        logger.warn ( "Disable addon: {}", addonName );

        final AddonRegistration addon = this.addons.get ( addonName );
        if ( addon != null )
        {
            addon.stop ();
        }
    }

    @Override
    public Addon install ( final URI uri )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized List<Addon> list ()
    {
        return this.addons.values ().stream ().map ( this::toAddon ).collect ( Collectors.toList () );
    }

    @Override
    public synchronized Optional<Addon> getAddon ( final String id )
    {
        return Optional.ofNullable ( this.addons.get ( id ) ).map ( this::toAddon );
    }

    private Addon toAddon ( final AddonRegistration registration )
    {
        if ( registration == null )
        {
            return null;
        }

        return new AddonImpl ( registration );
    }

}
