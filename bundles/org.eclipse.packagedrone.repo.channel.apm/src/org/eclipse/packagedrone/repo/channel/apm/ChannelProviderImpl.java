/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.apm;

import static org.eclipse.packagedrone.repo.channel.apm.ChannelModelProvider.makeBasePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ChannelOperationContext;
import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider;
import org.eclipse.packagedrone.repo.channel.provider.ProviderInformation;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An APM based channel provider
 * <p>
 * <em>Note: </em> the channel provider implementation is not thread safe. It
 * must be guaranteed by the caller (normally the ChannelService) that calls to
 * the channel provider implementation are mutually exclusive.
 * </p>
 */
public class ChannelProviderImpl implements ChannelProvider
{
    private final static Logger logger = LoggerFactory.getLogger ( ChannelProviderImpl.class );

    private static final String ID = "apm";

    private static final ProviderInformation INFO = new ProviderInformation ( ID, "APM storage", "APM based channel storage" );

    private StorageManager manager;

    private EventAdmin eventAdmin;

    private final CopyOnWriteArraySet<ChannelImpl> channels = new CopyOnWriteArraySet<> ();

    public ChannelProviderImpl ()
    {
    }

    public void setManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void setEventAdmin ( final EventAdmin eventAdmin )
    {
        this.eventAdmin = eventAdmin;
    }

    public void start ()
    {
    }

    public void stop ()
    {
        for ( final ChannelImpl channel : this.channels )
        {
            channel.dispose ();
        }
        this.channels.clear ();
    }

    @Override
    public Channel load ( final String channelId, final Map<MetaKey, String> configuration )
    {
        Objects.requireNonNull ( channelId );
        Objects.requireNonNull ( configuration );

        return new ChannelImpl ( channelId, this.eventAdmin, this.manager, this, configuration );
    }

    @Override
    public void create ( final String channelId, final Map<MetaKey, String> configuration )
    {
        Objects.requireNonNull ( channelId );
        Objects.requireNonNull ( configuration );

        createNewChannel ( channelId, configuration );
    }

    protected void createNewChannel ( final String channelId, final Map<MetaKey, String> configuration )
    {
        Objects.requireNonNull ( channelId );
        Objects.requireNonNull ( configuration );

        final ChannelImpl channel = new ChannelImpl ( channelId, this.eventAdmin, this.manager, this, configuration );
        try
        {
            channel.modifyRun ( model -> {
                // nothing .. just write the model for the first time
            } , ChannelOperationContext.NOOP );
        }
        finally
        {
            channel.dispose ();
        }
    }

    public void deleteChannel ( final ChannelImpl channel )
    {
        if ( this.channels.remove ( channel ) )
        {
            final String id = channel.getLocalId ();
            channel.dispose ();
            deleteChannelContent ( id );
        }
    }

    private void deleteChannelContent ( final String id )
    {
        Path path = makeBasePath ( this.manager.getContext (), id );

        final Path dir = path.getParent ();
        final Path fn = path.getFileSystem ().getPath ( "x-" + id );
        final Path target = dir == null ? fn : dir.resolve ( fn );
        try
        {
            Files.move ( path, target, StandardCopyOption.ATOMIC_MOVE );
            path = target;
        }
        catch ( final IOException e )
        {
            logger.warn ( "Failed to rename the channel directoy first", e );
        }

        try
        {
            Files.walkFileTree ( path, new RecursiveDeleteVisitor () );
        }
        catch ( final NoSuchFileException e )
        {
            // ignore
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to delete channel content", e );
        }
    }

    @Override
    public ProviderInformation getInformation ()
    {
        return INFO;
    }
}
