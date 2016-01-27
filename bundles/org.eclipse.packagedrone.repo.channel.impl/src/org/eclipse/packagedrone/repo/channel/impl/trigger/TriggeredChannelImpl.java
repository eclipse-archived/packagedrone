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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.impl.ChannelInstanceModel;
import org.eclipse.packagedrone.repo.channel.impl.ChannelInstanceModelAccess;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.TriggerConfiguration;
import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;
import org.eclipse.packagedrone.repo.trigger.TriggerHandle;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessor;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessorConfiguration;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessorState;
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;
import org.eclipse.packagedrone.utils.Holder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class TriggeredChannelImpl implements TriggeredChannel
{
    private static final String CONFIG_KEY = "triggers";

    private final ChannelId id;

    private final ChannelInstanceModel channelModel;

    private final ProcessorFactoryTracker processorFactoryTracker;

    private final Map<String, TriggerDescriptor> predefined;

    private final TriggeredChannelModel model;

    private boolean modified;

    public TriggeredChannelImpl ( final ChannelId id, final ChannelInstanceModel channelModel, final ProcessorFactoryTracker processorFactoryTracker, final Map<String, TriggerDescriptor> predefined )
    {
        this.id = id;
        this.channelModel = channelModel;
        this.processorFactoryTracker = processorFactoryTracker;
        this.predefined = predefined;

        this.model = loadConfiguration ( channelModel );
    }

    private static Gson createGson ()
    {
        final GsonBuilder gb = new GsonBuilder ();
        return gb.create ();
    }

    public static TriggeredChannelModel loadConfiguration ( final String cfg )
    {
        return createGson ().fromJson ( cfg, TriggeredChannelModel.class );
    }

    public static TriggeredChannelModel loadConfiguration ( final ChannelInstanceModelAccess model )
    {
        final String cfg = model.getConfigurations ().get ( CONFIG_KEY );
        if ( cfg != null )
        {
            return loadConfiguration ( cfg );
        }
        else
        {
            return new TriggeredChannelModel ();
        }
    }

    /**
     * Flush out the configuration to the channel model
     */
    public void flush ()
    {
        if ( this.modified )
        {
            this.channelModel.put ( CONFIG_KEY, createGson ().toJson ( this.model ) );
        }
    }

    @Override
    public ChannelId getId ()
    {
        return this.id;
    }

    @Override
    public Collection<TriggerHandle> listTriggers ()
    {
        final Set<String> triggers = new HashSet<> ();
        triggers.addAll ( this.predefined.keySet () );
        triggers.addAll ( this.model.getTriggers () );

        final Collection<TriggerHandle> result = new ArrayList<> ( triggers.size () );

        for ( final String triggerId : triggers )
        {
            final TriggerHandle handle = makeHandle ( triggerId );
            result.add ( handle );
        }

        return result;
    }

    @Override
    public Optional<TriggerHandle> getTrigger ( final String triggerId )
    {
        if ( triggerId == null )
        {
            return Optional.empty ();
        }

        if ( this.predefined.containsKey ( triggerId ) || this.model.getTriggers ().contains ( triggerId ) )
        {
            return Optional.of ( makeHandle ( triggerId ) );
        }
        return Optional.empty ();
    }

    private TriggerHandle makeHandle ( final String triggerId )
    {
        final Optional<TriggerDescriptor> descriptor = Optional.ofNullable ( TriggeredChannelImpl.this.predefined.get ( triggerId ) );
        final List<TriggerProcessorConfiguration> processorConfigurations = TriggeredChannelImpl.this.model.getProcessors ( triggerId );

        final List<TriggerProcessor> processors = processorConfigurations.stream ().map ( this::mapProcessor ).collect ( Collectors.toList () );

        return new TriggerHandle () {

            @Override
            public List<TriggerProcessor> getProcessors ()
            {
                return processors;
            }

            @Override
            public String getId ()
            {
                return triggerId;
            }

            @Override
            public Optional<TriggerDescriptor> getDescriptor ()
            {
                return descriptor;
            }

            @Override
            public Optional<TriggerConfiguration> getConfiguration ()
            {
                // FIXME: implement
                return Optional.empty ();
            }
        };
    }

    private TriggerProcessor mapProcessor ( final TriggerProcessorConfiguration cfg )
    {
        final Holder<Optional<TriggerProcessorState>> result = new Holder<> ();

        this.processorFactoryTracker.processOptionally ( cfg.getFactoryId (), factory -> {
            result.value = factory.map ( value -> value.validate ( cfg.getConfiguration () ) );
        } );

        return new TriggerProcessor ( cfg, result.value );
    }

    @Override
    public TriggerHandle addConfiguredTrigger ( final String triggerFactoryId, final String configuration )
    {
        this.modified = true;
        // FIXME: implement
        return null;
    }

    @Override
    public void modifyConfiguredTrigger ( final String triggerConfigurationId, final String configuration )
    {
        this.modified = true;
        // FIXME: implement
    }

    @Override
    public void deleteConfiguredTrigger ( final String triggerConfigurationId )
    {
        this.modified = true;

        // FIXME: implement
    }

    @Override
    public TriggerProcessorConfiguration addProcessor ( final String triggerId, final String processorFactoryId, final String configuration )
    {
        Objects.requireNonNull ( triggerId );
        Objects.requireNonNull ( processorFactoryId );

        final TriggerProcessorConfiguration result = new TriggerProcessorConfiguration ( UUID.randomUUID ().toString (), processorFactoryId, configuration );
        this.model.addProcessor ( triggerId, result );

        this.modified = true;

        return result;
    }

    @Override
    public void modifyProcessor ( final String triggerId, final String processorId, final String configuration )
    {
        Objects.requireNonNull ( triggerId );
        Objects.requireNonNull ( processorId );

        this.model.modifyProcessor ( triggerId, processorId, configuration );

        this.modified = true;
    }

    @Override
    public void deleteProcessor ( final String triggerId, final String processorId )
    {
        Objects.requireNonNull ( triggerId );
        Objects.requireNonNull ( processorId );

        this.model.deleteProcessor ( triggerId, processorId );

        this.modified = true;
    }

}
