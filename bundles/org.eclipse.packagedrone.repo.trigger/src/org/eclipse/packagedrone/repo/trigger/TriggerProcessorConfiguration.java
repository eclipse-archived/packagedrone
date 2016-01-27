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

public class TriggerProcessorConfiguration
{
    private final String id;

    private final String factoryId;

    private final String configuration;

    public TriggerProcessorConfiguration ( final String id, final String factoryId, final String configuration )
    {
        this.id = id;
        this.factoryId = factoryId;
        this.configuration = configuration;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getFactoryId ()
    {
        return this.factoryId;
    }

    public String getConfiguration ()
    {
        return this.configuration;
    }
}
