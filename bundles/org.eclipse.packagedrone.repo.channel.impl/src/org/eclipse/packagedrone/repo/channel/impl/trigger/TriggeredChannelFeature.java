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

import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.impl.ChannelFeature;
import org.eclipse.packagedrone.repo.channel.impl.ChannelInstanceModel;
import org.eclipse.packagedrone.repo.channel.impl.ChannelInstanceModelAccess;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessorConfiguration;
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.utils.Exceptions;

public class TriggeredChannelFeature implements ChannelFeature
{
    private final StorageManager storage;

    private final MetaKey storageKey;

    private final ProcessorFactoryTracker processorFactoryTracker;

    private final Map<String, TriggerDescriptor> predefined;

    public TriggeredChannelFeature ( final StorageManager storage, final MetaKey storageKey, final ProcessorFactoryTracker processorFactoryTracker, final Map<String, TriggerDescriptor> predefined )
    {
        this.storage = storage;
        this.storageKey = storageKey;
        this.processorFactoryTracker = processorFactoryTracker;
        this.predefined = predefined;
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
            throw new IllegalArgumentException ( "Channel feature supports only: " + TriggeredChannel.class.getName () );
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
        return new TriggeredChannelImpl ( id, model, this.processorFactoryTracker, this.predefined );
    }

}
