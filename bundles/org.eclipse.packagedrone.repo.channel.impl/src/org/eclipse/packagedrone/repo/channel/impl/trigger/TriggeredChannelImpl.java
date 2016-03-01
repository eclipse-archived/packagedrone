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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
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

    private final ConfiguredTriggerContext configuredContext;

    public TriggeredChannelImpl ( final ChannelId id, final ChannelInstanceModel channelModel, final ProcessorFactoryTracker processorFactoryTracker, final Map<String, TriggerDescriptor> predefined, final ConfiguredTriggerContext configuredContext )
    {
        this.id = id;
        this.channelModel = channelModel;
        this.processorFactoryTracker = processorFactoryTracker;
        this.predefined = predefined;
        this.configuredContext = configuredContext;

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
        Objects.requireNonNull ( model );

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
        final Set<String> triggers = new LinkedHashSet<> ();
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
        final Optional<TriggerDescriptor> descriptor = Optional.ofNullable ( findDescriptor ( triggerId ) );
        final List<TriggerProcessorConfiguration> processorConfigurations = TriggeredChannelImpl.this.model.getProcessors ( triggerId );

        final Optional<TriggerConfiguration> configuration = this.model.getTriggerConfiguration ( triggerId );

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
                return configuration;
            }

            @Override
            public TriggerHandle reorderProcessors ( final String processorId1, final String processorId2 ) throws IllegalArgumentException
            {
                TriggeredChannelImpl.this.reorderProcessors ( triggerId, processorId1, processorId2 );
                return makeHandle ( triggerId ); // create a new handle
            }

        };
    }

    private TriggerDescriptor findDescriptor ( final String triggerId )
    {
        final TriggerDescriptor result = this.predefined.get ( triggerId );
        if ( result != null )
        {
            return result;
        }

        return this.configuredContext.get ( triggerId );
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
        Objects.requireNonNull ( triggerFactoryId );

        final String triggerId = UUID.randomUUID ().toString ();
        final TriggerConfiguration cfg = new TriggerConfiguration ( triggerFactoryId, configuration );

        this.model.addTrigger ( triggerId, cfg );
        this.configuredContext.add ( triggerId, cfg );

        this.modified = true;

        return makeHandle ( triggerId );
    }

    @Override
    public void modifyConfiguredTrigger ( final String triggerConfigurationId, final String configuration )
    {
        Objects.requireNonNull ( triggerConfigurationId );

        this.model.modifyTrigger ( triggerConfigurationId, configuration );
        this.configuredContext.modify ( triggerConfigurationId, configuration );

        this.modified = true;
    }

    @Override
    public void deleteConfiguredTrigger ( final String triggerConfigurationId )
    {
        Objects.requireNonNull ( triggerConfigurationId );

        this.model.deleteTrigger ( triggerConfigurationId );
        this.configuredContext.dispose ( triggerConfigurationId );

        this.modified = true;
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

    private void reorderProcessors ( final String triggerId, final String processorId1, final String processorId2 )
    {
        this.model.reorderProcessors ( triggerId, processorId1, processorId2 );

        this.modified = true;
    }

    @Override
    public void reorder ( final String triggerId1, final String processorId1, final String triggerId2, final String processorId2 )
    {
        Objects.requireNonNull ( triggerId1 );
        Objects.requireNonNull ( triggerId2 );
        Objects.requireNonNull ( processorId1 );

        if ( triggerId1.equals ( triggerId2 ) )
        {
            reorderProcessors ( triggerId1, processorId1, processorId2 );
        }
        else
        {
            final Optional<TriggerHandle> t1 = getTrigger ( triggerId1 );
            final Optional<TriggerHandle> t2 = getTrigger ( triggerId2 );

            if ( !t1.isPresent () )
            {
                throw new IllegalArgumentException ( String.format ( "Unable to find trigger '%s'", triggerId1 ) );
            }
            if ( !t2.isPresent () )
            {
                throw new IllegalArgumentException ( String.format ( "Unable to find trigger '%s'", triggerId2 ) );
            }

            if ( !isCompatible ( t2.get (), t1.get ().getProcessor ( processorId1 ).orElseThrow ( unknownProcessor ( triggerId1, processorId1 ) ) ) )
            {
                throw new IllegalArgumentException ( String.format ( "Processor '%s' is not compatible with trigger '%s'", processorId1, triggerId2 ) );
            }

            this.model.moveProcessor ( triggerId1, processorId1, triggerId2, processorId2 );
            this.modified = true;
        }
    }

    private Supplier<? extends IllegalArgumentException> unknownProcessor ( final String triggerId, final String processorId )
    {
        return () -> new IllegalArgumentException ( String.format ( "Unable to find processor %s of trigger %s", processorId, triggerId ) );
    }

    private boolean isCompatible ( final TriggerHandle triggerHandle, final TriggerProcessor triggerProcessor )
    {
        final TriggerDescriptor desc = triggerHandle.getDescriptor ().orElseThrow ( () -> new IllegalStateException () );
        final TriggerProcessorState state = triggerProcessor.getState ().orElseThrow ( () -> new IllegalStateException ( String.format ( "Processor '%s' of trigger '%s' is not realized", triggerProcessor.getId (), triggerHandle.getId () ) ) );

        for ( final Class<?> clazz : desc.getSupportedContexts () )
        {
            if ( state.supportsContext ( clazz ) )
            {
                return true;
            }
        }

        return false;
    }

}
