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

import java.util.Objects;
import java.util.Optional;

public class TriggerProcessor
{
    private final TriggerProcessorConfiguration configuration;

    private final Optional<TriggerProcessorState> state;

    public TriggerProcessor ( final TriggerProcessorConfiguration configuration, final Optional<TriggerProcessorState> state )
    {
        Objects.requireNonNull ( configuration );
        Objects.requireNonNull ( state );

        this.configuration = configuration;
        this.state = state;
    }

    public TriggerProcessorConfiguration getConfiguration ()
    {
        return this.configuration;
    }

    public Optional<TriggerProcessorState> getState ()
    {
        return this.state;
    }

    public String getId ()
    {
        return this.configuration.getId ();
    }
}
