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
package org.eclipse.packagedrone.repo.channel.impl.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;

public class ChannelConfiguration
{
    private String providerId;

    private String description;

    private Map<MetaKey, String> configuration = new HashMap<> ();

    public ChannelConfiguration ()
    {
    }

    public ChannelConfiguration ( final ChannelConfiguration other )
    {
        this.providerId = other.providerId;
        this.description = other.description;
        this.configuration = new HashMap<> ( other.configuration );
    }

    public void setProviderId ( final String providerId )
    {
        this.providerId = providerId;
    }

    public String getProviderId ()
    {
        return this.providerId;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setConfiguration ( final Map<MetaKey, String> configuration )
    {
        this.configuration = configuration;
    }

    public Map<MetaKey, String> getConfiguration ()
    {
        return this.configuration;
    }
}
