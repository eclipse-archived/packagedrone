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
package org.eclipse.packagedrone.repo.channel.impl.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.impl.ChannelFeature;
import org.eclipse.packagedrone.repo.channel.impl.ChannelInstanceModel;
import org.eclipse.packagedrone.repo.channel.impl.ChannelInstanceModelAccess;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.TriggerConfiguration;
import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessorConfiguration;
import org.eclipse.packagedrone.repo.trigger.TriggerRunner;
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.utils.Exceptions;

public class TriggeredChannelFeature implements ChannelFeature, ConfiguredTriggerContext
{
    private final String channelId;

    private final StorageManager storage;

    private final MetaKey storageKey;

    private final ProcessorFactoryTracker processorFactoryTracker;

    private final ConfiguredTriggerFactoryTracker triggerFactory;

    private final Map<String, TriggerDescriptor> predefined;

    private final Map<String, ConfiguredTriggerInstance> configured = new HashMap<> ();

    public TriggeredChannelFeature ( final String channelId, final StorageManager storage, final MetaKey storageKey, final ProcessorFactoryTracker processorFactoryTracker, final ConfiguredTriggerFactoryTracker triggerFactory, final Map<String, TriggerDescriptor> predefined )
    {
        this.channelId = channelId;
        this.storage = storage;
        this.storageKey = storageKey;
        this.processorFactoryTracker = processorFactoryTracker;
        this.triggerFactory = triggerFactory;
        this.predefined = predefined;

        // initial load

        final TriggeredChannelModel cfg = storage.accessCall ( storageKey, ChannelInstanceModelAccess.class, TriggeredChannelImpl::loadConfiguration );
        for ( final Map.Entry<String, TriggerConfiguration> tc : cfg.getConfiguredTriggers ().entrySet () )
        {
            addConfiguredTrigger ( tc.getKey (), tc.getValue () );
        }
    }

    @Override
    public boolean supportsAccess ( final Class<?> clazz )
    {
        return TriggeredChannel.class.equals ( clazz );
    }

    @SuppressWarnings ( "unchecked" )
    @Override
    public <T, R> R access ( final ChannelId id, final Class<T> clazz, final ChannelOperation<R, T> operation ) throws Exception
    {
        if ( !supportsAccess ( clazz ) )
        {
            throw new IllegalArgumentException ( String.format ( "Channel feature supports only: %s", TriggeredChannel.class.getName () ) );
        }

        return this.storage.modifyCall ( this.storageKey, ChannelInstanceModel.class, model -> {
            return Exceptions.wrapException ( () -> {

                final TriggeredChannelImpl adapter = makeTriggeredChannel ( id, model );

                final R result = operation.process ( (T)adapter );

                // only flush when there was no exception
                adapter.flush ();

                return result;
            } );
        } );

    }

    public List<TriggerProcessorConfiguration> getProcessors ( final String triggerId )
    {
        return this.storage.accessCall ( this.storageKey, ChannelInstanceModelAccess.class, TriggeredChannelImpl::loadConfiguration ).getProcessors ( triggerId );
    }

    private TriggeredChannelImpl makeTriggeredChannel ( final ChannelId id, final ChannelInstanceModel model )
    {
        return new TriggeredChannelImpl ( id, model, this.processorFactoryTracker, this.predefined, this );
    }

    @Override
    public void add ( final String id, final TriggerConfiguration configuration )
    {
        StorageManager.executeAfterPersist ( () -> {
            synchronized ( this.configured )
            {
                addConfiguredTrigger ( id, configuration );
            }
        } );
    }

    private void addConfiguredTrigger ( final String id, final TriggerConfiguration configuration )
    {
        final Consumer<Object> runner = ( context ) -> runTrigger ( id, context );
        this.configured.put ( id, new ConfiguredTriggerInstance ( this.channelId, this.triggerFactory, configuration, runner ) );
    }

    private void runTrigger ( final String id, final Object context )
    {
        final List<TriggerProcessorConfiguration> processors = getProcessors ( id );

        if ( processors != null )
        {
            new TriggerRunner ( this.processorFactoryTracker, processors, context ).run ();
        }
    }

    @Override
    public void modify ( final String id, final String configuration )
    {
        StorageManager.executeAfterPersist ( () -> {
            synchronized ( this.configured )
            {
                final ConfiguredTriggerInstance instance = this.configured.get ( id );
                if ( instance != null )
                {
                    instance.update ( configuration );
                }
            }
        } );
    }

    @Override
    public void dispose ( final String id )
    {
        StorageManager.executeAfterPersist ( () -> {
            final ConfiguredTriggerInstance instance;
            synchronized ( this.configured )
            {
                instance = this.configured.remove ( id );
            }
            if ( instance != null )
            {
                instance.dispose ();
            }
        } );
    }

    @Override
    public TriggerDescriptor get ( final String triggerId )
    {
        synchronized ( this.configured )
        {
            final ConfiguredTriggerInstance instance = this.configured.get ( triggerId );
            if ( instance != null )
            {
                return instance.getState ();
            }
            return null;
        }
    }

}
