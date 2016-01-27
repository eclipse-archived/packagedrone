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

import java.util.Collection;
import java.util.Optional;

import org.eclipse.packagedrone.repo.channel.ChannelId;

public interface TriggeredChannel
{
    public ChannelId getId ();

    public Collection<TriggerHandle> listTriggers ();

    public Optional<TriggerHandle> getTrigger ( String triggerId );

    // configured triggered

    public TriggerHandle addConfiguredTrigger ( String triggerFactoryId, String configuration );

    public void modifyConfiguredTrigger ( String triggerConfigurationId, String configuration );

    public void deleteConfiguredTrigger ( String triggerConfigurationId );

    // processors

    public TriggerProcessorConfiguration addProcessor ( String triggerId, String processorFactoryId, String configuration );

    public void modifyProcessor ( String triggerId, String processorId, String configuration );

    public void deleteProcessor ( String triggerId, String processorId );
}
