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
package org.eclipse.packagedrone.repo.trigger.web;

import java.util.List;

import org.eclipse.packagedrone.repo.trigger.ProcessorFactoryInformation;
import org.eclipse.packagedrone.repo.trigger.TriggerConfiguration;
import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;
import org.eclipse.packagedrone.repo.trigger.TriggerHandle;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessor;

public class TriggerEntry
{
    private final TriggerHandle handle;

    private final List<ProcessorFactoryInformation> availableProcessors;

    public TriggerEntry ( final TriggerHandle handle, final List<ProcessorFactoryInformation> availableProcessors )
    {
        this.handle = handle;
        this.availableProcessors = availableProcessors;
    }

    public TriggerHandle getHandle ()
    {
        return this.handle;
    }

    public String getId ()
    {
        return this.handle.getId ();
    }

    public List<ProcessorFactoryInformation> getAvailableProcessors ()
    {
        return this.availableProcessors;
    }

    public List<TriggerProcessor> getProcessors ()
    {
        return this.handle.getProcessors ();
    }

    public TriggerDescriptor getDescriptor ()
    {
        return this.handle.getDescriptor ().orElse ( null );
    }

    public TriggerConfiguration getConfiguration ()
    {
        return this.handle.getConfiguration ().orElse ( null );
    }

}
