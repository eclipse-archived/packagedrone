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

import static org.eclipse.packagedrone.repo.utils.Splits.split;
import static org.eclipse.packagedrone.utils.Locks.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

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
import org.eclipse.packagedrone.repo.channel.provider.AccessContext;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;
import org.eclipse.packagedrone.repo.channel.provider.ProviderInformation;
import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider.Listener;
import org.eclipse.packagedrone.repo.channel.stats.ChannelStatistics;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.StorageRegistration;
import org.eclipse.packagedrone.utils.Locks.Locked;
import org.eclipse.scada.utils.str.Tables;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ChannelServiceImpl implements ChannelService, DeployAuthService
{

    private class Entry implements Listener
    {
        private final ChannelProvider service;

        private final Map<String, Channel> channels = new HashMap<> ();

        public Entry ( final ChannelProvider service )
        {
            this.service = service;

            this.service.addListener ( this );

            addProvider ( service );
        }

        public void dispose ()
        {
            removeProvider ( this.service );

            this.service.removeListener ( this );
            handleUpdate ( this.service, null, this.channels.values () );
            this.channels.clear ();
        }

        @Override
        public void update ( final Collection<? extends Channel> added, final Collection<? extends Channel> removed )
        {
            if ( added != null )
            {
                added.forEach ( channel -> this.channels.put ( channel.getId (), channel ) );
            }
            if ( removed != null )
            {
                removed.forEach ( channel -> this.channels.remove ( channel.getId () ) );
            }
            handleUpdate ( this.service, added, removed );
        }

    }

    private static final MetaKey KEY_STORAGE = new MetaKey ( "channels", "service" );

    private final BundleContext context;

    private final ServiceTrackerCustomizer<ChannelProvider, Entry> customizer = new ServiceTrackerCustomizer<ChannelProvider, ChannelServiceImpl.Entry> () {

        @Override
        public Entry addingService ( final ServiceReference<ChannelProvider> reference )
        {
            return new Entry ( ChannelServiceImpl.this.context.getService ( reference ) );
        }

        @Override
        public void modifiedService ( final ServiceReference<ChannelProvider> reference, final Entry service )
        {
        }

        @Override
        public void removedService ( final ServiceReference<ChannelProvider> reference, final Entry service )
        {
            service.dispose ();
        }

    };

    /**
     * Used to read-lock the channel and provider map
     */
    private final Lock readLock;

    /**
     * Used to write-lock the channel and provider map
     */
    private final Lock writeLock;

    private final ServiceTracker<ChannelProvider, Entry> tracker;

    private final Map<String, ChannelEntry> channelMap = new HashMap<> ();

    private final Map<String, ChannelProvider> providerMap = new HashMap<> ();

    private final Set<ProviderInformation> providers = new CopyOnWriteArraySet<> ();

    private final Set<ProviderInformation> unmodProviders = Collections.unmodifiableSet ( this.providers );

    private StorageManager manager;

    private StorageRegistration handle;

    /**
     * Map channel ids to deploy groups, cache
     */
    private final Multimap<String, DeployGroup> deployKeysMap = HashMultimap.create ();

    private ChannelAspectProcessor aspectProcessor;

    public ChannelServiceImpl ()
    {
        this.context = FrameworkUtil.getBundle ( ChannelServiceImpl.class ).getBundleContext ();

        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock ();

        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();

        this.tracker = new ServiceTracker<ChannelProvider, Entry> ( this.context, ChannelProvider.class, this.customizer );
    }

    public void setStorageManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void handleUpdate ( final ChannelProvider provider, final Collection<? extends Channel> added, final Collection<? extends Channel> removed )
    {
        try ( final Locked l = lock ( this.writeLock ) )
        {
            // process additions

            if ( added != null )
            {
                added.forEach ( channel -> {
                    final String mappedId = makeMappedId ( provider, channel );
                    final String name = mapName ( mappedId );
                    this.channelMap.put ( mappedId, new ChannelEntry ( new ChannelId ( mappedId, name ), channel, provider ) );
                } );
            }

            // process removals

            if ( removed != null )
            {
                removed.forEach ( channel -> {
                    final String mappedId = makeMappedId ( provider, channel );
                    this.channelMap.remove ( mappedId );
                } );
            }
        }
    }

    private String mapName ( final String mappedId )
    {
        return this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, model -> model.mapToName ( mappedId ) );
    }

    private static String makeMappedId ( final ChannelProvider provider, final Channel channel )
    {
        return makeMappedId ( provider.getId (), channel.getId () );
    }

    private static String makeMappedId ( final String providerId, final String channelId )
    {
        return String.format ( "%s_%s", providerId, channelId );
    }

    public void start ()
    {
        this.aspectProcessor = new ChannelAspectProcessor ( FrameworkUtil.getBundle ( ChannelService.class ).getBundleContext () );

        this.handle = this.manager.registerModel ( 1_000, KEY_STORAGE, new ChannelServiceModelProvider () );

        this.manager.accessRun ( KEY_STORAGE, ChannelServiceAccess.class, ( model ) -> updateDeployGroupCache ( model ) );

        try ( Locked l = lock ( this.writeLock ) )
        {
            this.tracker.open ();
        }
    }

    public void stop ()
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.tracker.close ();
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
    }

    private static ChannelInformation accessState ( final ChannelEntry channelEntry )
    {
        return accessRead ( channelEntry, channel -> channel.getInformation () );
    }

    @Override
    public Collection<ChannelInformation> list ()
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return this.channelMap.values ().stream ().map ( ChannelServiceImpl::accessState ).collect ( Collectors.toList () );
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
    protected Optional<ChannelEntry> find ( final By by )
    {
        switch ( by.getType () )
        {
            case ID:
                return Optional.ofNullable ( this.channelMap.get ( by.getQualifier () ) );
            case NAME:
                return findByName ( (String)by.getQualifier () );
            case COMPOSITE:
            {
                final By[] bys = (By[])by.getQualifier ();
                for ( final By oneBy : bys )
                {
                    final Optional<ChannelEntry> result = find ( oneBy );
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

    /**
     * Find a channel by name
     *
     * @param name
     *            the channel name to look for
     * @return the optional channel entry, never returns {@code null} but my
     *         return {@link Optional#empty()}.
     */
    private Optional<ChannelEntry> findByName ( final String name )
    {
        if ( name == null )
        {
            return Optional.empty ();
        }

        // FIXME: improve performance

        for ( final ChannelEntry entry : this.channelMap.values () )
        {
            if ( name.equals ( entry.getId ().getName () ) )
            {
                return Optional.of ( entry );
            }
        }

        return Optional.empty ();
    }

    @Override
    public Optional<ChannelInformation> getState ( final By by )
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return find ( by ).map ( ChannelServiceImpl::accessState );
        }
    }

    public void addProvider ( final ChannelProvider provider )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            final ProviderInformation info = provider.getInformation ();

            this.providerMap.put ( info.getId (), provider );
            this.providers.add ( info );
        }
    }

    public void removeProvider ( final ChannelProvider provider )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            final ProviderInformation info = provider.getInformation ();

            this.providerMap.remove ( info.getId () );
            this.providers.remove ( info );
        }
    }

    @Override
    public Collection<ProviderInformation> getProviders ()
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return this.unmodProviders;
        }
    }

    @Override
    public ChannelId create ( final String providerId, final ChannelDetails description )
    {
        ChannelProvider provider;
        try ( Locked l = lock ( this.readLock ) )
        {
            if ( providerId != null )
            {
                provider = this.providerMap.get ( providerId );
            }
            else if ( this.providerMap.size () == 1 )
            {
                provider = this.providerMap.values ().iterator ().next ();
            }
            else
            {
                throw new IllegalArgumentException ( "No provider selected, but there is more than one provider available." );
            }
        }

        final Channel channel = provider.create ( description, localId -> makeMappedId ( providerId, localId ) );

        final String id = makeMappedId ( provider, channel );
        return new ChannelId ( id, mapName ( id ) );
    }

    @Override
    public boolean delete ( final By by )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            final Optional<ChannelEntry> channel = find ( by );
            if ( !channel.isPresent () )
            {
                return false;
            }

            final ChannelEntry entry = channel.get ();

            // explicitly delete the mapping

            deleteChannel ( entry.getId ().getId () );
            handleUpdate ( entry.getProvider (), null, Collections.singleton ( entry.getChannel () ) );

            entry.getChannel ().delete ();

            return true;
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

    private static <R> R accessRead ( final ChannelEntry channelEntry, final ChannelOperation<R, ReadableChannel> operation )
    {
        return channelEntry.getChannel ().accessCall ( ctx -> {

            try ( Disposing<AccessContext> wrappedCtx = Disposing.proxy ( AccessContext.class, ctx );
                  Disposing<ReadableChannel> channel = Disposing.proxy ( ReadableChannel.class, new ReadableChannelAdapter ( channelEntry.getId (), wrappedCtx.getTarget () ) ) )
            {
                return operation.process ( channel.getTarget () );
            }
        } , localId -> makeMappedId ( channelEntry.getProvider ().getId (), localId ) );
    }

    private <T, R> R accessModify ( final ChannelEntry channelEntry, final ChannelOperation<R, ModifiableChannel> operation )
    {
        return channelEntry.getChannel ().modifyCall ( ctx -> {
            try ( Disposing<ModifyContext> wrappedCtx = Disposing.proxy ( ModifyContext.class, ctx );
                  Disposing<ModifiableChannel> channel = Disposing.proxy ( ModifiableChannel.class, new ModifiableChannelAdapter ( channelEntry.getId (), wrappedCtx.getTarget (), this.aspectProcessor ) ) )
            {
                return operation.process ( channel.getTarget () );
            }
        } , localId -> makeMappedId ( channelEntry.getProvider ().getId (), localId ) );
    }

    private <R> R handleDeployKeys ( final ChannelEntry channel, final ChannelOperation<R, DeployKeysChannelAdapter> operation )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {
                final DeployKeysChannelAdapterImpl adapter = new DeployKeysChannelAdapterImpl ( channel.getId ().getId (), model) {

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

    private <R, T> R runDisposing ( final ChannelOperation<R, T> operation, final Class<T> clazz, final T target )
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
            final Optional<ChannelEntry> channelEntry = find ( by );

            if ( !channelEntry.isPresent () )
            {
                return Optional.empty ();
            }

            return Optional.ofNullable ( this.deployKeysMap.get ( channelEntry.get ().getId ().getId () ) ).map ( Collections::unmodifiableCollection );
        }
    }

    private <R> R handleDescribe ( final ChannelEntry channelEntry, final ChannelOperation<R, DescriptorAdapter> operation )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {

                final DescriptorAdapter dai = new DescriptorAdapterImpl ( channelEntry) {
                    @Override
                    public void setName ( final String name )
                    {
                        model.putMapping ( getDescriptor ().getId (), name );

                        super.setName ( name );

                        final ChannelId desc = getDescriptor ();
                        StorageManager.executeAfterPersist ( () -> {
                            channelEntry.setId ( desc );
                        } );
                    }
                };

                return runDisposing ( operation, DescriptorAdapter.class, dai );
            } );
        }
    }

    private ChannelEntry findChannel ( final By by )
    {
        final Optional<ChannelEntry> channel;
        try ( Locked l = lock ( this.readLock ) )
        {
            channel = find ( by );
        }

        if ( !channel.isPresent () )
        {
            throw new ChannelNotFoundException ( "fixme" );
        }

        return channel.get ();
    }

    @Override
    public Map<String, String> getUnclaimedMappings ()
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {

                final Map<String, String> map = model.getNameMap ();

                for ( final ChannelEntry entry : this.channelMap.values () )
                {
                    map.remove ( entry.getId () );
                }

                return map;
            } );
        }
    }

    /**
     * This is a console command
     */
    public void listUnclaimedMappings ()
    {
        final Map<String, String> map = getUnclaimedMappings ();

        final List<List<String>> rows = new ArrayList<> ( map.size () );

        for ( final Map.Entry<String, String> entry : map.entrySet () )
        {
            final ArrayList<String> row = new ArrayList<> ( 2 );
            row.add ( entry.getKey () );
            row.add ( entry.getValue () );
            rows.add ( row );
        }

        Tables.showTable ( System.out, Arrays.asList ( "ID", "Name" ), rows, 2 );
    }

    public void deleteChannel ( final String channelId )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.manager.modifyRun ( KEY_STORAGE, ChannelServiceModify.class, model -> {

                model.deleteChannel ( channelId );

                StorageManager.executeAfterPersist ( () -> {

                    // try to remove from mapped channels
                    final ChannelEntry channel = this.channelMap.get ( channelId );
                    if ( channel != null )
                    {
                        channel.setId ( new ChannelId ( channelId, null ) );
                    }

                    // remove deploy groups for channel
                    this.deployKeysMap.removeAll ( channelId );
                } );
            } );

        }
    }

    @Override
    public void deleteMapping ( final String id, final String name )
    {
        try ( final Locked l = lock ( this.writeLock ) )
        {
            this.manager.modifyRun ( KEY_STORAGE, ChannelServiceModify.class, model -> internalDeleteMapping ( id, name, model ) );
        }
    }

    private void internalDeleteMapping ( final String id, final String name, final ChannelServiceModify model )
    {
        final String affectedId = model.deleteMapping ( id, name );
        if ( affectedId != null )
        {
            StorageManager.executeAfterPersist ( () -> {
                final ChannelEntry channel = this.channelMap.get ( id );
                // try to remove from mapped channels
                if ( channel != null )
                {
                    channel.setId ( new ChannelId ( id, null ) );
                }
            } );
        }
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

    @Override
    public void wipeClean ()
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.manager.modifyRun ( KEY_STORAGE, ChannelServiceModify.class, model -> {
                wipeChannelService ( model );
            } );
            for ( final ChannelProvider provider : this.providerMap.values () )
            {
                provider.wipe ();
            }
        }
    }

    private void wipeChannelService ( final ChannelServiceModify model )
    {
        model.clear ();
        StorageManager.executeAfterPersist ( () -> {

            // wipe all names

            for ( final ChannelEntry entry : this.channelMap.values () )
            {
                entry.setId ( new ChannelId ( entry.getId ().getId (), null ) );
            }
        } );
    }

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
