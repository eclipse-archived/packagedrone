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
package org.eclipse.packagedrone.repo.channel.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.channel.AspectableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.PreAddContext;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.impl.trigger.TriggerDescriptorImpl;
import org.eclipse.packagedrone.repo.channel.impl.trigger.TriggeredChannelFeature;
import org.eclipse.packagedrone.repo.channel.provider.AccessContext;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ChannelOperationContext;
import org.eclipse.packagedrone.repo.channel.provider.ChannelProvider;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;
import org.eclipse.packagedrone.repo.trigger.TriggerRunner;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.StorageRegistration;
import org.eclipse.packagedrone.utils.Locks;
import org.eclipse.packagedrone.utils.Locks.Locked;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelInstance implements ProviderListener
{
    private final static Logger logger = LoggerFactory.getLogger ( ChannelInstance.class );

    private static final String TRIGGER_ID_PRE_ADD = "pre-add";

    private static final String TRIGGER_ID_POST = "post";

    private Channel channel;

    private final String channelId;

    private final String providerId;

    private final Map<MetaKey, String> configuration;

    private final ChannelProviderTracker providerTracker;

    private boolean disposed;

    private final Lock readLock;

    private final Lock writeLock;

    private final ChannelAspectProcessor aspectProcessor;

    private final EventAdmin eventAdmin;

    private final Set<ChannelFeature> features = new HashSet<> ();

    private final StorageManager storage;

    private final StorageRegistration handle;

    private final TriggeredChannelFeature triggered;

    private final ProcessorFactoryTracker processorFactoryTracker;

    public ChannelInstance ( final String channelId, final String providerId, final Map<MetaKey, String> configuration, final ChannelProviderTracker providerTracker, final ChannelAspectProcessor aspectProcessor, final EventAdmin eventAdmin, final StorageManager storage, final ProcessorFactoryTracker processorFactoryTracker )
    {
        this.channelId = channelId;
        this.providerId = providerId;
        this.configuration = configuration;

        this.providerTracker = providerTracker;

        this.aspectProcessor = aspectProcessor;
        this.eventAdmin = eventAdmin;
        this.storage = storage;
        this.processorFactoryTracker = processorFactoryTracker;

        final ReadWriteLock lock = new ReentrantReadWriteLock ( false );
        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();

        final MetaKey channelKey = new MetaKey ( "channelInstance", channelId );
        this.handle = this.storage.registerModel ( 15_000, channelKey, new ChannelInstanceModelProvider ( channelId ) );

        try
        {
            addFeature ( this.triggered = new TriggeredChannelFeature ( storage, channelKey, processorFactoryTracker, makePredefinedTriggers () ) );

            this.providerTracker.addListener ( this.providerId, this );
        }
        catch ( final Exception e )
        {
            this.handle.unregister ();
            throw e;
        }
    }

    private static Map<String, TriggerDescriptor> makePredefinedTriggers ()
    {
        final Map<String, TriggerDescriptor> result = new HashMap<> ();

        result.put ( TRIGGER_ID_PRE_ADD, new TriggerDescriptorImpl ( "Before addition", "This trigger is fired before an artifact is being added.", PreAddContext.class ) );
        result.put ( TRIGGER_ID_POST, new TriggerDescriptorImpl ( "Post operation", "This trigger is fired after each channel operation.", ModifiableChannel.class ) );

        return result;
    }

    public void dispose ()
    {
        try ( Locked l = Locks.lock ( this.writeLock ) )
        {
            if ( this.disposed )
            {
                // already disposed
                return;
            }
            this.disposed = true;

            if ( this.handle != null )
            {
                this.handle.unregister ();
            }

            this.providerTracker.removeListener ( this.providerId, this );
            unbind ();
        }
    }

    public void addFeature ( final ChannelFeature feature )
    {
        this.features.add ( feature );
    }

    public void removeFeature ( final ChannelFeature feature )
    {
        this.features.remove ( feature );
    }

    public String getChannelId ()
    {
        return this.channelId;
    }

    public String getProviderId ()
    {
        return this.providerId;
    }

    public Map<MetaKey, String> getConfiguration ()
    {
        return this.configuration;
    }

    @Deprecated
    public Optional<Channel> getChannel ()
    {
        return Locks.call ( this.readLock, () -> Optional.ofNullable ( this.channel ) );
    }

    @Override
    public void bind ( final ChannelProvider provider )
    {
        logger.debug ( "Binding to provider: {}", provider );

        try ( Locked l = Locks.lock ( this.writeLock ) )
        {
            this.channel = provider.load ( this.channelId, this.configuration );
        }
    }

    @Override
    public void unbind ()
    {
        logger.debug ( "Unbinding from provider" );

        try ( Locked l = Locks.lock ( this.writeLock ) )
        {
            if ( this.channel != null )
            {
                this.channel.dispose ();
                this.channel = null;
            }
        }
    }

    @SuppressWarnings ( "unchecked" )
    public <R, T> R access ( final ChannelId id, final Class<T> clazz, final ChannelOperation<R, T> operation )
    {
        // try built in

        if ( ReadableChannel.class.equals ( clazz ) )
        {
            return access ( id, (ChannelOperation<R, ReadableChannel>)operation );
        }
        else if ( ModifiableChannel.class.equals ( clazz ) )
        {
            return modify ( id, makeChannelOperationContext (), this.aspectProcessor, (ChannelOperation<R, ModifiableChannel>)operation );
        }
        else if ( AspectableChannel.class.equals ( clazz ) )
        {
            return modify ( id, makeChannelOperationContext (), this.aspectProcessor, (ChannelOperation<R, ModifiableChannel>)operation );
        }

        // try features

        for ( final ChannelFeature feature : this.features )
        {
            if ( feature.supportsAccess ( clazz ) )
            {
                try
                {
                    return feature.access ( id, clazz, operation );
                }
                catch ( final RuntimeException e )
                {
                    throw e;
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
            }
        }

        // no more options

        throw new IllegalArgumentException ( String.format ( "Unknown channel adapter: %s", clazz.getName () ) );
    }

    public <R> R access ( final ChannelId id, final ChannelOperation<R, ReadableChannel> operation )
    {
        try ( Locked l = Locks.lock ( this.readLock ) )
        {
            final Channel channel = this.channel != null ? this.channel : new ErrorChannel ( unboundMessage () );

            return channel.accessCall ( ctx -> {

                try ( Disposing<AccessContext> wrappedCtx = Disposing.proxy ( AccessContext.class, ctx );
                      Disposing<ReadableChannel> channelInterface = Disposing.proxy ( ReadableChannel.class, new ReadableChannelAdapter ( id, wrappedCtx.getTarget () ) ) )
                {
                    return operation.process ( channelInterface.getTarget () );
                }
            } );
        }
    }

    public <R> R modify ( final ChannelId id, final ChannelOperationContext context, final ChannelAspectProcessor aspectProcessor, final ChannelOperation<R, ModifiableChannel> operation )
    {
        try ( Locked l = Locks.lock ( this.readLock ) )
        {
            if ( this.channel == null )
            {
                throw new IllegalStateException ( unboundMessage () );
            }

            return this.channel.modifyCall ( ctx -> {
                try ( Disposing<ModifyContext> wrappedCtx = Disposing.proxy ( ModifyContext.class, ctx );
                      Disposing<ModifiableChannel> channelInterface = Disposing.proxy ( ModifiableChannel.class, new ModifiableChannelAdapter ( id, wrappedCtx.getTarget (), aspectProcessor ) ) )
                {
                    final R result = operation.process ( channelInterface.getTarget () );

                    runPostTrigger ( channelInterface.getTarget () );

                    return result;
                }
            } , context );
        }
    }

    private void runPreAddTrigger ( final PreAddContext ctx )
    {
        runTrigger ( TRIGGER_ID_PRE_ADD, ctx );
    }

    private void runPostTrigger ( final ModifiableChannel ctx )
    {
        runTrigger ( TRIGGER_ID_POST, ctx );
    }

    private void runTrigger ( final String triggerId, final Object ctx )
    {
        new TriggerRunner ( this.processorFactoryTracker, this.triggered.getProcessors ( triggerId ), ctx ).run ();
    }

    private String unboundMessage ()
    {
        return String.format ( "Channel '%s' is not bound to provider '%s'", this.channelId, this.providerId );
    }

    private ChannelOperationContext makeChannelOperationContext ()
    {
        return new ChannelOperationContext () {

            @Override
            public void artifactPreAdd ( final PreAddContext ctx )
            {
                runPreAddTrigger ( ctx );
            }

            @Override
            public void postAspectOperation ( final String aspectId, final String operation )
            {
                final Map<String, Object> data = new HashMap<> ( 2 );
                data.put ( "operation", operation );
                data.put ( "aspectFactoryId", aspectId );

                ChannelInstance.this.eventAdmin.postEvent ( new Event ( String.format ( "drone/channel/%s/aspect", makeSafeTopic ( ChannelInstance.this.channelId ) ), data ) );
            }

            private String makeSafeTopic ( final String aspectId )
            {
                return aspectId.replaceAll ( "[^a-zA-Z0-9_\\-]", "_" );
            }
        };
    }

}
