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
package org.eclipse.packagedrone.repo.channel.impl;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.eclipse.packagedrone.repo.utils.Splits.split;
import static org.eclipse.packagedrone.utils.Locks.lock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.channel.AspectableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.DeployKeysChannelAdapter;
import org.eclipse.packagedrone.repo.channel.DescriptorAdapter;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.deploy.DeployAuthService;
import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.deploy.DeployKey;
import org.eclipse.packagedrone.repo.channel.impl.model.ChannelConfiguration;
import org.eclipse.packagedrone.repo.channel.provider.AccessContext;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;
import org.eclipse.packagedrone.repo.channel.stats.ChannelStatistics;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.StorageRegistration;
import org.eclipse.packagedrone.utils.Locks.Locked;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ChannelServiceImpl implements ChannelService, DeployAuthService
{
    private static final MetaKey KEY_STORAGE = new MetaKey ( "channels", "service" );

    private final BundleContext context;

    /**
     * Used to read-lock the channel and provider map
     */
    private final Lock readLock;

    /**
     * Used to write-lock the channel and provider map
     */
    private final Lock writeLock;

    private StorageManager manager;

    private StorageRegistration handle;

    /**
     * Map channel ids to deploy groups, cache
     */
    private final Multimap<String, DeployGroup> deployKeysMap = HashMultimap.create ();

    private ChannelAspectProcessor aspectProcessor;

    private final ChannelProviderTracker providerTracker;

    private final Map<String, ChannelInstance> channels = new HashMap<> ();

    public ChannelServiceImpl ()
    {
        this.context = FrameworkUtil.getBundle ( ChannelServiceImpl.class ).getBundleContext ();

        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock ();

        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();

        this.providerTracker = new ChannelProviderTracker ( this.context );
    }

    public void setStorageManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void start ()
    {
        this.aspectProcessor = new ChannelAspectProcessor ( this.context );
        this.providerTracker.start ();

        this.handle = this.manager.registerModel ( 1_000, KEY_STORAGE, new ChannelServiceModelProvider () );

        this.manager.accessRun ( KEY_STORAGE, ChannelServiceAccess.class, model -> {
            updateDeployGroupCache ( model );

            for ( final Map.Entry<String, ChannelConfiguration> entry : model.getChannels ().entrySet () )
            {
                loadChannel ( entry.getKey (), entry.getValue () );
            }
        } );
    }

    private void loadChannel ( @NonNull final String channelId, @NonNull final ChannelConfiguration configuration )
    {
        this.channels.put ( channelId, new ChannelInstance ( channelId, configuration.getProviderId (), configuration.getConfiguration (), this.providerTracker ) );
    }

    public void stop ()
    {
        this.channels.values ().stream ().forEach ( ChannelInstance::dispose );
        this.channels.clear ();

        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }

        if ( this.aspectProcessor != null )
        {
            this.aspectProcessor.close ();
            this.aspectProcessor = null;
        }

        this.providerTracker.stop ();
    }

    private ChannelInformation accessState ( final ChannelInstance channelEntry )
    {
        return accessRead ( channelEntry, channel -> channel.getInformation () );
    }

    @Override
    public Collection<ChannelInformation> list ()
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return this.channels.values ().stream ().map ( this::accessState ).collect ( Collectors.toList () );
        }
    }

    /**
     * Find by the locator
     * <p>
     * This method does not acquire the read lock, this has to be done by the
     * caller
     * </p>
     *
     * @param by
     *            the locator
     * @return the result
     */
    protected Optional<ChannelInstance> find ( final By by )
    {
        switch ( by.getType () )
        {
            case ID:
                return findById ( (String)by.getQualifier () );
            case NAME:
                return findByName ( (String)by.getQualifier () );
            case COMPOSITE:
            {
                final By[] bys = (By[])by.getQualifier ();
                for ( final By oneBy : bys )
                {
                    final Optional<ChannelInstance> result = find ( oneBy );
                    if ( result.isPresent () )
                    {
                        return result;
                    }
                }
                return Optional.empty ();
            }
            default:
                throw new IllegalArgumentException ( String.format ( "Unknown locator type: %s", by.getType () ) );
        }
    }

    private @NonNull Optional<@Nullable ChannelInstance> findById ( @Nullable final String id )
    {
        if ( id == null )
        {
            return empty ();
        }

        return ofNullable ( this.channels.get ( id ) );
    }

    /**
     * Find a channel by name
     *
     * @param name
     *            the channel name to look for
     * @return the optional channel entry, never returns {@code null} but my
     *         return {@link Optional#empty()}.
     */
    private @NonNull Optional<ChannelInstance> findByName ( @Nullable final String name )
    {
        if ( name == null )
        {
            return empty ();
        }

        final String id = this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, channels -> {
            return channels.mapToId ( name );
        } );

        return findById ( id );
    }

    @Override
    public Optional<ChannelInformation> getState ( final By by )
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return find ( by ).map ( this::accessState );
        }
    }

    @Override
    public ChannelId create ( @NonNull final String providerId, @NonNull final ChannelDetails details, @NonNull final Map<MetaKey, String> configuration )
    {
        final String channelId = UUID.randomUUID ().toString ();

        final ChannelConfiguration cfg = new ChannelConfiguration ();
        cfg.setProviderId ( providerId );
        cfg.setConfiguration ( configuration );
        cfg.setDescription ( details.getDescription () );

        this.providerTracker.run ( providerId, p -> {
            final ChannelProvider provider = p.orElseThrow ( () -> new IllegalStateException ( String.format ( "Channel provider '%s' is not registered", providerId ) ) );
            provider.create ( channelId, configuration );
        } );

        this.manager.accessRun ( KEY_STORAGE, ChannelServiceModify.class, channels -> {
            channels.createChannel ( channelId, cfg );
        } );

        // FIXME: ensure that we are the only active call to the storage manager model KEY_STORAGE
        commitCreateChannel ( channelId, providerId, configuration );

        return new ChannelId ( channelId );
    }

    private void commitCreateChannel ( @NonNull final String channelId, @NonNull final String providerId, @NonNull final Map<MetaKey, String> configuration )
    {
        final ChannelInstance channel = new ChannelInstance ( channelId, providerId, configuration, this.providerTracker );

        try ( Locked l = lock ( this.writeLock ) )
        {
            this.channels.put ( channelId, channel );
        }
    }

    @SuppressWarnings ( "unchecked" )
    @Override
    public <R, T> R accessCall ( final By by, final Class<T> clazz, final ChannelOperation<R, T> operation )
    {
        if ( ReadableChannel.class.equals ( clazz ) )
        {
            return accessRead ( findChannel ( by ), (ChannelOperation<R, ReadableChannel>)operation );
        }
        else if ( ModifiableChannel.class.equals ( clazz ) )
        {
            return accessModify ( findChannel ( by ), (ChannelOperation<R, ModifiableChannel>)operation );
        }
        else if ( DeployKeysChannelAdapter.class.equals ( clazz ) )
        {
            return handleDeployKeys ( findChannel ( by ), (ChannelOperation<R, DeployKeysChannelAdapter>)operation );
        }
        else if ( DescriptorAdapter.class.equals ( clazz ) )
        {
            return handleDescribe ( findChannel ( by ), (ChannelOperation<R, DescriptorAdapter>)operation );
        }
        else if ( AspectableChannel.class.equals ( clazz ) )
        {
            return accessModify ( findChannel ( by ), (ChannelOperation<R, ModifiableChannel>)operation );
        }
        else
        {
            throw new IllegalArgumentException ( String.format ( "Unknown channel adapter: %s", clazz.getName () ) );
        }
    }

    private <R> R accessRead ( final ChannelInstance channelEntry, final ChannelOperation<R, ReadableChannel> operation )
    {
        return this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, channels -> {

            final ChannelId id = buildId ( channelEntry, channels );

            @SuppressWarnings ( "null" )
            @NonNull
            final Channel channelInstance = (@NonNull Channel)channelEntry.getChannel ().orElse ( new ErrorChannel ( unboundMessage ( channelEntry ) ) );

            return channelInstance.accessCall ( ctx -> {

                try ( Disposing<AccessContext> wrappedCtx = Disposing.proxy ( AccessContext.class, ctx );
                      Disposing<ReadableChannel> channel = Disposing.proxy ( ReadableChannel.class, new ReadableChannelAdapter ( id, wrappedCtx.getTarget () ) ) )
                {
                    return operation.process ( channel.getTarget () );
                }
            } );
        } );
    }

    private <R> R accessModify ( final ChannelInstance channelEntry, final ChannelOperation<R, ModifiableChannel> operation )
    {
        return this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, channels -> {

            final ChannelId id = buildId ( channelEntry, channels );

            if ( !channelEntry.getChannel ().isPresent () )
            {
                throw new IllegalStateException ( unboundMessage ( channelEntry ) );
            }

            return channelEntry.getChannel ().get ().modifyCall ( ctx -> {
                try ( Disposing<ModifyContext> wrappedCtx = Disposing.proxy ( ModifyContext.class, ctx );
                      Disposing<ModifiableChannel> channel = Disposing.proxy ( ModifiableChannel.class, new ModifiableChannelAdapter ( id, wrappedCtx.getTarget (), this.aspectProcessor ) ) )
                {
                    return operation.process ( channel.getTarget () );
                }
            } );
        } );
    }

    private static String unboundMessage ( final ChannelInstance channelEntry )
    {
        return String.format ( "Channel '%s' is not bound to provider '%s'", channelEntry.getChannelId (), channelEntry.getProviderId () );
    }

    private ChannelId buildId ( final ChannelInstance channelEntry, final ChannelServiceAccess channels )
    {
        final String channelId = channelEntry.getChannelId ();
        final Set<String> names = new LinkedHashSet<> ( channels.getNameMappings ( channelId ) );
        final String description = channels.getDescription ( channelId );
        return new ChannelId ( channelId, names, description );
    }

    private <R> R handleDeployKeys ( final ChannelInstance channel, final ChannelOperation<R, DeployKeysChannelAdapter> operation )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {

                final DeployKeysChannelAdapterImpl adapter = new DeployKeysChannelAdapterImpl ( channel.getChannelId (), model) {

                    @Override
                    public void assignDeployGroup ( final String groupId )
                    {
                        super.assignDeployGroup ( groupId );
                        StorageManager.executeAfterPersist ( () -> {
                            updateDeployGroupCache ( this.model );
                        } );
                    }

                    @Override
                    public void unassignDeployGroup ( final String groupId )
                    {
                        super.unassignDeployGroup ( groupId );
                        StorageManager.executeAfterPersist ( () -> {
                            updateDeployGroupCache ( this.model );
                        } );
                    }

                };
                return runDisposing ( operation, DeployKeysChannelAdapter.class, adapter );
            } );
        }
    }

    private static <R, T> R runDisposing ( final ChannelOperation<R, T> operation, final Class<T> clazz, final T target )
    {
        try ( Disposing<T> adapter = Disposing.proxy ( clazz, target ) )
        {
            return operation.process ( adapter.getTarget () );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    @Override
    public @NonNull Optional<Collection<DeployGroup>> getChannelDeployGroups ( final By by )
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            final Optional<ChannelInstance> channel = find ( by );

            if ( !channel.isPresent () )
            {
                return Optional.empty ();
            }

            return Optional.ofNullable ( this.deployKeysMap.get ( channel.get ().getChannelId () ) ).map ( Collections::unmodifiableCollection );
        }
    }

    private <R> R handleDescribe ( final ChannelInstance channelEntry, final ChannelOperation<R, DescriptorAdapter> operation )
    {
        return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {
            final DescriptorAdapter dai = new DescriptorAdapterImpl ( channelEntry.getChannelId (), model );
            return runDisposing ( operation, DescriptorAdapter.class, dai );
        } );
    }

    private ChannelInstance findChannel ( final By by )
    {
        final Optional<ChannelInstance> channel;
        try ( Locked l = lock ( this.readLock ) )
        {
            channel = find ( by );
        }

        if ( !channel.isPresent () )
        {
            throw new ChannelNotFoundException ( by.toString () );
        }

        return channel.get ();
    }

    @Override
    public boolean delete ( final By by )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            final Optional<ChannelInstance> channelInstance = find ( by );
            if ( !channelInstance.isPresent () )
            {
                return false;
            }

            final ChannelInstance entry = channelInstance.get ();

            final Optional<Channel> channel = entry.getChannel ();
            if ( !channel.isPresent () )
            {
                throw new IllegalStateException ( String.format ( "Can only delete channel %s when the provider %s is present", entry.getChannelId (), entry.getProviderId () ) );
            }

            deleteChannel ( entry );

            channel.get ().delete ();

            return true;
        }
    }

    protected void deleteChannel ( final ChannelInstance channel )
    {
        this.manager.modifyRun ( KEY_STORAGE, ChannelServiceModify.class, model -> {

            model.deleteChannel ( channel.getChannelId () );

            StorageManager.executeAfterPersist ( () -> commitDeleteChannel ( channel ) );
        } );
    }

    @Override
    public void wipeClean ()
    {
        // get the current list of channels

        List<ChannelInstance> channels;
        try ( Locked l = lock ( this.writeLock ) )
        {
            channels = new ArrayList<> ( this.channels.values () );
        }

        // delete one by one

        for ( final ChannelInstance instance : channels )
        {
            final Optional<Channel> channel = instance.getChannel ();
            if ( channel.isPresent () )
            {
                // ... only if the provider is present

                deleteChannel ( instance );
                this.channels.remove ( instance.getChannelId () );
                channel.get ().delete ();
            }
        }
    }

    private void commitDeleteChannel ( final ChannelInstance channel )
    {
        // remove the channel from the map
        this.channels.remove ( channel.getChannelId () );

        // remove deploy groups for channel
        this.deployKeysMap.removeAll ( channel.getChannelId () );
    }

    /**
     * Update the channel to deploy group cache map
     *
     * @param model
     *            the model to fill the cache from
     */
    private void updateDeployGroupCache ( final ChannelServiceAccess model )
    {
        // this will simply rebuild the complete map

        // clear first
        this.deployKeysMap.clear ();

        // fill afterwards
        for ( final Map.Entry<String, Set<String>> entry : model.getDeployGroupMap ().entrySet () )
        {
            final String channelId = entry.getKey ();
            final List<DeployGroup> groups = entry.getValue ().stream ().map ( groupId -> model.getDeployGroup ( groupId ) ).collect ( Collectors.toList () );
            this.deployKeysMap.putAll ( channelId, groups );
        }
    }

    // methods of DeployAuthService

    @Override
    public List<DeployGroup> listGroups ( final int position, final int count )
    {
        return this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, model -> {
            return split ( model.getDeployGroups (), position, count );
        } );
    }

    @Override
    public DeployGroup createGroup ( final String name )
    {
        return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {
            return model.createGroup ( name );
        } );
    }

    @Override
    public void deleteGroup ( final String groupId )
    {
        try ( final Locked l = lock ( this.writeLock ) )
        {
            this.manager.modifyRun ( KEY_STORAGE, ChannelServiceModify.class, model -> {
                model.deleteGroup ( groupId );
                StorageManager.executeAfterPersist ( () -> {
                    updateDeployGroupCache ( model );
                } );
            } );
        }
    }

    @Override
    public void updateGroup ( final String groupId, final String name )
    {
        try ( final Locked l = lock ( this.writeLock ) )
        {
            this.manager.modifyRun ( KEY_STORAGE, ChannelServiceModify.class, model -> {
                model.updateGroup ( groupId, name );
                StorageManager.executeAfterPersist ( () -> {
                    updateDeployGroupCache ( model );
                } );
            } );
        }
    }

    @Override
    public DeployGroup getGroup ( final String groupId )
    {
        return this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, model -> {
            return model.getDeployGroup ( groupId );
        } );
    }

    @Override
    public DeployKey createDeployKey ( final String groupId, final String name )
    {
        return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {
            StorageManager.executeAfterPersist ( () -> {
                updateDeployGroupCache ( model );
            } );
            return model.createKey ( groupId, name );
        } );
    }

    @Override
    public DeployKey deleteDeployKey ( final String keyId )
    {
        return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {
            StorageManager.executeAfterPersist ( () -> {
                updateDeployGroupCache ( model );
            } );
            return model.deleteKey ( keyId );
        } );
    }

    @Override
    public DeployKey getDeployKey ( final String keyId )
    {
        return this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, model -> {
            return model.getDeployKey ( keyId );
        } );
    }

    @Override
    public DeployKey updateDeployKey ( final String keyId, final String name )
    {
        return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {
            StorageManager.executeAfterPersist ( () -> {
                updateDeployGroupCache ( model );
            } );
            return model.updateKey ( keyId, name );
        } );
    }

    // statistics

    @Override
    public ChannelStatistics getStatistics ()
    {
        final ChannelStatistics cs = new ChannelStatistics ();

        try ( Locked l = lock ( this.readLock ) )
        {
            final Collection<ChannelInformation> cis = list ();

            cs.setTotalNumberOfArtifacts ( cis.stream ().mapToLong ( ci -> ci.getState ().getNumberOfArtifacts () ).sum () );
            cs.setTotalNumberOfBytes ( cis.stream ().mapToLong ( ci -> ci.getState ().getNumberOfBytes () ).sum () );
        }

        return cs;
    }

}
