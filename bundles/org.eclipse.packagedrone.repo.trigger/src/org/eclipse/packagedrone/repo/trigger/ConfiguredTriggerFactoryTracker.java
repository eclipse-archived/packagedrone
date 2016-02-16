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
package org.eclipse.packagedrone.repo.trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.packagedrone.utils.Holder;
import org.eclipse.packagedrone.utils.osgi.FactoryTracker;
import org.eclipse.packagedrone.utils.osgi.SimpleFactoryTracker;
import org.osgi.framework.BundleContext;

public class ConfiguredTriggerFactoryTracker
{
    public static final String PROP_ID = "drone.trigger.factory.id"; //$NON-NLS-1$

    private static class ConfiguredTriggerFactoryInformationExt extends ConfiguredTriggerFactoryInformation
    {
        private final ConfiguredTriggerFactory factory;

        public ConfiguredTriggerFactoryInformationExt ( final String id, final ConfiguredTriggerFactory factory )
        {
            super ( id, factory.getLabel (), factory.getDescription (), factory.getConfigurationUrl () );
            this.factory = factory;
        }

        public ConfiguredTriggerFactory getFactory ()
        {
            return this.factory;
        }
    }

    private final SimpleFactoryTracker<ConfiguredTriggerFactory, ConfiguredTriggerFactoryInformationExt> tracker;

    public ConfiguredTriggerFactoryTracker ( final BundleContext context )
    {
        this.tracker = new SimpleFactoryTracker<> ( context, ConfiguredTriggerFactory.class, ref -> FactoryTracker.getString ( ref, PROP_ID ), ( ref, service ) -> {
            return new ConfiguredTriggerFactoryInformationExt ( FactoryTracker.getString ( ref, PROP_ID ), service );
        } );
        this.tracker.open ();
    }

    public void close ()
    {
        this.tracker.close ();
    }

    public void process ( final String factoryId, final Consumer<ConfiguredTriggerFactory> factory )
    {
        this.tracker.consume ( factoryId, entry -> factory.accept ( entry.getFactory () ) );
    }

    public void processOptionally ( final String factoryId, final Consumer<Optional<ConfiguredTriggerFactory>> factory )
    {
        this.tracker.consumeOptionally ( factoryId, entry -> factory.accept ( entry.map ( ConfiguredTriggerFactoryInformationExt::getFactory ) ) );
    }

    public Optional<ConfiguredTriggerFactoryInformation> getFactoryInformation ( final String factoryId )
    {
        final Holder<Optional<ConfiguredTriggerFactoryInformation>> result = new Holder<> ( Optional.empty () );
        this.tracker.consumeOptionally ( factoryId, factory -> result.value = Optional.ofNullable ( factory.orElse ( null ) ) );
        return result.value;
    }

    public List<ConfiguredTriggerFactoryInformation> getFactoryInformations ()
    {
        final List<ConfiguredTriggerFactoryInformation> result = new ArrayList<> ();
        this.tracker.consumeAll ( stream -> stream.forEach ( result::add ) );
        return result;
    }

    public void addListener ( final String factoryId, final Consumer<ConfiguredTriggerFactory> consumer )
    {
        this.tracker.addListener ( factoryId, wrapListener ( consumer ) );
    }

    public void removeListener ( final String factoryId, final Consumer<ConfiguredTriggerFactory> consumer )
    {
        this.tracker.removeListener ( factoryId, wrapListener ( consumer ) );
    }

    private Consumer<ConfiguredTriggerFactoryInformationExt> wrapListener ( final Consumer<ConfiguredTriggerFactory> consumer )
    {
        Objects.requireNonNull ( consumer );

        return new Consumer<ConfiguredTriggerFactoryTracker.ConfiguredTriggerFactoryInformationExt> () {

            @Override
            public void accept ( final ConfiguredTriggerFactoryInformationExt t )
            {
                consumer.accept ( t.getFactory () );
            }

            @Override
            public int hashCode ()
            {
                return consumer.hashCode ();
            }

            @Override
            public boolean equals ( final Object obj )
            {
                return consumer.equals ( obj );
            }
        };
    }

}
