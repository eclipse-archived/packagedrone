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

public class TriggerConfiguration
{
    private final String triggerFactoryId;

    private final String configuration;

    public TriggerConfiguration ( final String triggerFactoryId, final String configuration )
    {
        this.triggerFactoryId = triggerFactoryId;
        this.configuration = configuration;
    }

    public String getTriggerFactoryId ()
    {
        return this.triggerFactoryId;
    }

    public String getConfiguration ()
    {
        return this.configuration;
    }

}
