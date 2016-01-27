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

public class ProcessorFactoryInformation
{
    private final String id;

    private final String description;

    private final String label;

    private final String configurationUrl;

    public ProcessorFactoryInformation ( final String id, final String label, final String description, final String configurationUrl )
    {
        this.id = id;
        this.label = label;
        this.description = description;
        this.configurationUrl = configurationUrl;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public String getConfigurationUrl ()
    {
        return this.configurationUrl;
    }
}
