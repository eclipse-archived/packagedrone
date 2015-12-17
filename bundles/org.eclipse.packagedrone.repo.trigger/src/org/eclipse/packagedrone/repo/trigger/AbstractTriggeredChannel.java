/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.trigger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.BundleContext;

public abstract class AbstractTriggeredChannel implements TriggeredChannel
{
    private final class TriggerInstanceImpl implements ConfigurableTriggerInstance
    {
        private final String triggerId;

        private final String triggerFactoryId;

        private TriggerInstanceImpl ( final String triggerId, final String triggerFactoryId, final Map<String, String> configuration )
        {
            this.triggerId = triggerId;
            this.triggerFactoryId = triggerFactoryId;
        }

        @Override
        public void configure ( final Map<String, String> configuration )
        {
            processUpdate ( new TriggerConfiguration ( this.triggerId, this.triggerFactoryId, new HashMap<> ( configuration ) ) );
            // FIXME: update trigger
        }

        @Override
        public void delete ()
        {
            processDelete ( this.triggerId );
        }

        @Override
        public @NonNull Optional<Trigger> getTrigger ()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @NonNull Map<String, String> getConfiguration ()
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static final class TriggerConfiguration
    {
        private String id;

        private String factoryId;

        private Map<String, String> configuration;

        public TriggerConfiguration ()
        {
        }

        public TriggerConfiguration ( final String id, final String factoryId, final Map<String, String> configuration )
        {
            this.id = id;
            this.factoryId = factoryId;
            this.configuration = configuration;
        }

        public String getId ()
        {
            return this.id;
        }

        public void setId ( final String id )
        {
            this.id = id;
        }

        public String getFactoryId ()
        {
            return this.factoryId;
        }

        public void setFactoryId ( final String factoryId )
        {
            this.factoryId = factoryId;
        }

        public Map<String, String> getConfiguration ()
        {
            return this.configuration;
        }

        public void setConfiguration ( final Map<String, String> configuration )
        {
            this.configuration = configuration;
        }
    }

    private final ReadLock readLock;

    private final WriteLock writeLock;

    private final Map<String, ConfigurableTriggerInstance> triggers = new HashMap<> ();

    public AbstractTriggeredChannel ( final BundleContext context )
    {
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock ();

        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();
    }

    public void dispose ()
    {
    }

    protected void bind ( final String factoryId, final TriggerFactory factory )
    {
    }

    protected void unbind ( final String factoryId, final TriggerFactory factory )
    {
    }

    protected void initialize ( final List<TriggerConfiguration> configurations )
    {
        // process stored configurations

        for ( final TriggerConfiguration cfg : configurations )
        {
            initializeTrigger ( cfg );
        }
    }

    private void initializeTrigger ( final TriggerConfiguration cfg )
    {
        this.triggers.put ( cfg.getId (), new TriggerInstanceImpl ( cfg.getId (), cfg.getFactoryId (), cfg.getConfiguration () ) );
    }

    protected abstract void storeConfiguration ( TriggerConfiguration configuration );

    protected abstract void deleteConfiguration ( String id );

    @Override
    public ConfigurableTriggerInstance createTrigger ( final String triggerFactoryId, Map<String, String> configuration )
    {
        final String triggerId = UUID.randomUUID ().toString ();
        configuration = new HashMap<> ( configuration ); // clone

        // do a first check for the trigger factory

        findFactory ( triggerFactoryId ).orElseThrow ( () -> new IllegalArgumentException ( String.format ( "Unable to find factory '%s'", triggerFactoryId ) ) );

        final ConfigurableTriggerInstance trigger = new TriggerInstanceImpl ( triggerId, triggerFactoryId, configuration );

        storeConfiguration ( new TriggerConfiguration ( triggerId, triggerFactoryId, configuration ) );
        this.triggers.put ( triggerId, trigger );
        return trigger;
    }

    private @NonNull Optional<TriggerFactory> findFactory ( final String triggerFactoryId )
    {
        return Optional.empty ();
    }

    protected void processUpdate ( final TriggerConfiguration cfg )
    {
        storeConfiguration ( cfg );
    }

    protected void processDelete ( final String id )
    {
        deleteConfiguration ( id );
        this.triggers.remove ( id );
    }

    @Override
    public Map<String, TriggerInstance> listTriggers ()
    {
        return Collections.unmodifiableMap ( new HashMap<> ( this.triggers ) );
    }
}
