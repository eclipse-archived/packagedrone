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

import java.util.List;

public class TriggerRunner
{
    private final List<TriggerProcessorConfiguration> processors;

    private final ProcessorFactoryTracker factoryTracker;

    private final Object context;

    public TriggerRunner ( final ProcessorFactoryTracker factoryTracker, final List<TriggerProcessorConfiguration> processors, final Object context )
    {
        this.factoryTracker = requireNonNull ( factoryTracker );
        this.processors = requireNonNull ( processors );
        this.context = requireNonNull ( context );
    }

    public void run ()
    {
        this.processors.stream ().forEachOrdered ( this::runProcessor );
    }

    private void runProcessor ( final TriggerProcessorConfiguration cfg )
    {
        final String factoryId = cfg.getFactoryId ();
        this.factoryTracker.process ( factoryId, factory -> runProcessor ( factory, factoryId, cfg.getConfiguration () ) );
    }

    private void runProcessor ( final ProcessorFactory factory, final String factoryId, final String configuration )
    {
        if ( !factory.supportsContext ( this.context.getClass () ) )
        {
            throw new IllegalStateException ( String.format ( "Trying to call processor '%s' with context class '%s', which is not supported by the processor", factoryId, this.context.getClass ().getName () ) );
        }

        final Processor processor = factory.create ( configuration );
        processor.process ( this.context );
    }
}
