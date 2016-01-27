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

import static java.util.Objects.requireNonNull;
import static org.eclipse.packagedrone.utils.osgi.FactoryTracker.getString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.packagedrone.utils.Holder;
import org.eclipse.packagedrone.utils.osgi.FactoryTracker;
import org.eclipse.packagedrone.utils.osgi.SimpleFactoryTracker;
import org.osgi.framework.BundleContext;

public class ProcessorFactoryTracker
{
    public static final String PROP_ID = "drone.trigger.processor.factory.id"; //$NON-NLS-1$

    private static class ProcessorFactoryInformationExt extends ProcessorFactoryInformation
    {
        private final ProcessorFactory factory;

        public ProcessorFactoryInformationExt ( final String id, final ProcessorFactory factory )
        {
            super ( id, factory.getLabel (), factory.getDescription (), factory.getConfigurationUrl () );
            this.factory = factory;
        }

        public ProcessorFactory getFactory ()
        {
            return this.factory;
        }
    }

    private final SimpleFactoryTracker<ProcessorFactory, ProcessorFactoryInformationExt> tracker;

    public ProcessorFactoryTracker ( final BundleContext context )
    {
        this.tracker = new SimpleFactoryTracker<> ( context, ProcessorFactory.class, ref -> FactoryTracker.getString ( ref, PROP_ID ), ( ref, service ) -> {
            return new ProcessorFactoryInformationExt ( getString ( ref, PROP_ID ), service );
        } );
        this.tracker.open ();
    }

    public void close ()
    {
        this.tracker.close ();
    }

    public void process ( final String factoryId, final Consumer<ProcessorFactory> factory )
    {
        this.tracker.consume ( factoryId, entry -> factory.accept ( entry.getFactory () ) );
    }

    public void processOptionally ( final String factoryId, final Consumer<Optional<ProcessorFactory>> factory )
    {
        this.tracker.consumeOptionally ( factoryId, entry -> factory.accept ( entry.map ( ProcessorFactoryInformationExt::getFactory ) ) );
    }

    /**
     * Get a list of matching factories
     * <p>
     * The returned list is a copy of the current state. The list may be
     * modified by the caller, but will not be updated by this tracker when the
     * state changes.
     * </p>
     *
     * @param contextClasses
     *            the provided context classes
     * @return a list of matching processor factors, never returns {@code null}
     */
    public List<ProcessorFactoryInformation> getFactoriesFor ( final Class<?>[] contextClasses )
    {
        requireNonNull ( contextClasses );

        final List<ProcessorFactoryInformation> result = new ArrayList<> ();
        this.tracker.consumeAll ( stream -> stream.filter ( service -> isMatch ( service, contextClasses ) ).forEach ( result::add ) );
        return result;
    }

    public Optional<ProcessorFactoryInformation> getFactoryInformation ( final String factoryId )
    {
        final Holder<Optional<ProcessorFactoryInformation>> result = new Holder<> ( Optional.empty () );
        this.tracker.consumeOptionally ( factoryId, factory -> result.value = Optional.ofNullable ( factory.orElse ( null ) ) );
        return result.value;
    }

    private boolean isMatch ( final ProcessorFactoryInformationExt service, final Class<?>[] contextClasses )
    {
        if ( service == null || contextClasses == null || contextClasses.length <= 0 )
        {
            return false;
        }

        final ProcessorFactory factory = service.getFactory ();

        for ( final Class<?> contextClass : contextClasses )
        {
            if ( contextClass == null )
            {
                continue;
            }

            if ( factory.supportsContext ( contextClass ) )
            {
                return true;
            }
        }

        return false;
    }
}
