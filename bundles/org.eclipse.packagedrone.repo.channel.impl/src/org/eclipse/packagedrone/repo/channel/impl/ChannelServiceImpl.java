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
package org.eclipse.packagedrone.repo.channel.impl;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.eclipse.packagedrone.repo.utils.Splits.split;
import static org.eclipse.packagedrone.utils.Locks.lock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.DeployKeysChannelAdapter;
import org.eclipse.packagedrone.repo.channel.DescriptorAdapter;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.deploy.DeployAuthService;
import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.deploy.DeployKey;
import org.eclipse.packagedrone.repo.channel.impl.model.ChannelConfiguration;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider;
import org.eclipse.packagedrone.repo.channel.stats.ChannelStatistics;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactoryTracker;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.StorageRegistration;
import org.eclipse.packagedrone.utils.Locks.Locked;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventAdmin;

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

    private ChannelProviderTracker providerTracker;

    private final Map<String, ChannelInstance> channels = new HashMap<> ();

    private EventAdmin eventAdmin;

    private ProcessorFactoryTracker processorFactoryTracker;

    private ConfiguredTriggerFactoryTracker triggerFactoryTracker;

    public ChannelServiceImpl ()
    {
        this.context = FrameworkUtil.getBundle ( ChannelServiceImpl.class ).getBundleContext ();

        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock ();

        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();
    }

    public void setEventAdmin ( final EventAdmin eventAdmin )
    {
        this.eventAdmin = eventAdmin;
    }

    public void setStorageManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void start ()
    {
        this.aspectProcessor = new ChannelAspectProcessor ( this.context );

        this.providerTracker = new ChannelProviderTracker ( this.context );
        this.providerTracker.start ();

        this.processorFactoryTracker = new ProcessorFactoryTracker ( this.context );
        this.triggerFactoryTracker = new ConfiguredTriggerFactoryTracker ( this.context );

        this.handle = this.manager.registerModel ( 1_000, KEY_STORAGE, new ChannelServiceModelProvider () );

        try
        {
            this.manager.accessRun ( KEY_STORAGE, ChannelServiceAccess.class, model -> {
                updateDeployGroupCache ( model );

                for ( final Map.Entry<String, ChannelConfiguration> entry : model.getChannels ().entrySet () )
                {
                    loadChannel ( entry.getKey (), entry.getValue () );
                }
            } );
        }
        catch ( final Exception e )
        {
            dispose ();
            throw e;
        }
    }

    public void stop ()
    {
        dispose ();
    }

    private void dispose ()
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.channels.values ().stream ().forEach ( ChannelInstance::dispose );
            this.channels.clear ();
        }

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

        if ( this.providerTracker != null )
        {
            this.providerTracker.stop ();
            this.providerTracker = null;
        }

        if ( this.processorFactoryTracker != null )
        {
            this.processorFactoryTracker.close ();
            this.processorFactoryTracker = null;
        }

        if ( this.triggerFactoryTracker != null )
        {
            this.triggerFactoryTracker.close ();
            this.triggerFactoryTracker = null;
        }
    }

    private void loadChannel ( final String channelId, final ChannelConfiguration configuration )
    {
        this.channels.put ( channelId, new ChannelInstance ( channelId, configuration.getProviderId (), configuration.getConfiguration (), this.providerTracker, this.aspectProcessor, this.eventAdmin, this.manager, this.processorFactoryTracker, this.triggerFactoryTracker ) );
    }

    private ChannelInformation accessState ( final ChannelServiceAccess channels, final ChannelInstance channel )
    {
        return channel.access ( buildId ( channel.getChannelId (), channels ), ReadableChannel.class, ReadableChannel::getInformation );
    }

    @Override
    public Collection<ChannelInformation> list ()
    {
        return accessChannels ( channels -> {
            return this.channels.values ().stream ().map ( instance -> accessState ( channels, instance ) ).collect ( Collectors.toList () );
        } );
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

    private Optional<ChannelInstance> findById ( final String id )
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
    private Optional<ChannelInstance> findByName ( final String name )
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
        return accessChannels ( channels -> {
            return find ( by ).map ( instance -> accessState ( channels, instance ) );
        } );
    }

    @Override
    public ChannelId create ( final String providerId, final ChannelDetails details, final Map<MetaKey, String> configuration )
    {
        final String channelId = UUID.randomUUID ().toString ();

        final String actualProviderId;
        if ( providerId != null )
        {
            actualProviderId = providerId;
        }
        else
        {
            actualProviderId = "apm";
        }

        final ChannelConfiguration cfg = new ChannelConfiguration ();
        cfg.setProviderId ( actualProviderId );
        cfg.setConfiguration ( configuration );
        cfg.setDescription ( details.getDescription () );

        this.providerTracker.run ( actualProviderId, p -> {
            final ChannelProvider provider = p.orElseThrow ( () -> new IllegalStateException ( String.format ( "Channel provider '%s' is not registered", actualProviderId ) ) );
            provider.create ( channelId, configuration );
        } );

        this.manager.accessRun ( KEY_STORAGE, ChannelServiceModify.class, channels -> {
            channels.createChannel ( channelId, cfg );
        } );

        // FIXME: ensure that we are the only active call to the storage manager model KEY_STORAGE
        commitCreateChannel ( channelId, actualProviderId, configuration );

        return new ChannelId ( channelId );
    }

    private void commitCreateChannel ( final String channelId, final String providerId, final Map<MetaKey, String> configuration )
    {
        final ChannelInstance channel = new ChannelInstance ( channelId, providerId, configuration, this.providerTracker, this.aspectProcessor, this.eventAdmin, this.manager, this.processorFactoryTracker, this.triggerFactoryTracker );

        try ( Locked l = lock ( this.writeLock ) )
        {
            this.channels.put ( channelId, channel );
        }
    }

    private <R> R accessChannels ( final Function<ChannelServiceAccess, R> operation )
    {
        return this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, channels -> {
            return operation.apply ( channels );
        } );
    }

    private <R> R modifyChannels ( final Function<ChannelServiceModify, R> operation )
    {
        return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, channels -> {
            return operation.apply ( channels );
        } );
    }

    private <R, T> R modifyChannel ( final By by, final Function<ChannelInstance, R> operation )
    {
        return modifyChannels ( channels -> {
            final ChannelInstance channelInstance = findChannel ( by );
            return operation.apply ( channelInstance );
        } );
    }

    @SuppressWarnings ( "unchecked" )
    @Override
    public <R, T> R accessCall ( final By by, final Class<T> clazz, final ChannelOperation<R, T> operation )
    {
        if ( DeployKeysChannelAdapter.class.equals ( clazz ) )
        {
            return modifyChannel ( by, channelInstance -> handleDeployKeys ( channelInstance, (ChannelOperation<R, DeployKeysChannelAdapter>)operation ) );
        }
        else if ( DescriptorAdapter.class.equals ( clazz ) )
        {
            return modifyChannel ( by, channelInstance -> handleDescribe ( channelInstance, (ChannelOperation<R, DescriptorAdapter>)operation ) );
        }

        return accessChannels ( channels -> {
            final ChannelInstance channelInstance = findChannel ( by );
            final ChannelId id = buildId ( channelInstance.getChannelId (), channels );
            return channelInstance.access ( id, clazz, operation );
        } );
    }

    private static ChannelId buildId ( final String channelId, final ChannelServiceAccess channels )
    {
        final Set<String> names = new LinkedHashSet<> ( channels.getNameMappings ( channelId ) );
        final String description = channels.getDescription ( channelId );
        return new ChannelId ( channelId, names, description );
    }

    private <R> R handleDeployKeys ( final ChannelInstance channel, final ChannelOperation<R, DeployKeysChannelAdapter> operation )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {

                final DeployKeysChannelAdapterImpl adapter = new DeployKeysChannelAdapterImpl ( channel.getChannelId (), model ) {

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
    public Optional<Collection<DeployGroup>> getChannelDeployGroups ( final By by )
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            final Optional<ChannelInstance> channel = find ( by );

            if ( !channel.isPresent () )
            {
                return Optional.empty ();
            }

            return Optional.ofNullable ( this.deployKeysMap.get ( channel.get ().getChannelId () ) ).map ( ArrayList<DeployGroup>::new );
        }
    }

    private <R> R handleDescribe ( final ChannelInstance channelEntry, final ChannelOperation<R, DescriptorAdapter> operation )
    {
        return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {
            final DescriptorAdapter dai = new DescriptorAdapterImpl ( channelEntry.getChannelId (), model );
            return runDisposing ( operation, DescriptorAdapter.class, dai );
        } );
    }

    /**
     * Find a channel
     *
     * @param by
     *            the search criteria
     * @return the found channel, never returns {@code null}
     * @throws ChannelNotFoundException
     *             if there was no channel found
     */
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

            final ChannelInstance instance = channelInstance.get ();

            final Optional<Channel> channel = instance.getChannel ();
            if ( !channel.isPresent () )
            {
                throw new IllegalStateException ( String.format ( "Can only delete channel %s when the provider %s is present", instance.getChannelId (), instance.getProviderId () ) );
            }

            deleteChannel ( instance );

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
