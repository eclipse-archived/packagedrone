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
package org.eclipse.packagedrone.repo.channel.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChannelInstanceModel implements ChannelInstanceModelAccess
{
    private final Map<String, String> configurations;

    private transient final Map<String, String> configurationsUnmod;

    private boolean modified;

    public ChannelInstanceModel ()
    {
        this.configurations = new HashMap<> ();
        this.configurationsUnmod = Collections.unmodifiableMap ( this.configurations );
    }

    public ChannelInstanceModel ( final ChannelInstanceModel other )
    {
        this.configurations = new HashMap<> ( other.configurations );
        this.configurationsUnmod = Collections.unmodifiableMap ( this.configurations );
    }

    @Override
    public Map<String, String> getConfigurations ()
    {
        return this.configurationsUnmod;
    }

    public boolean isModified ()
    {
        return this.modified;
    }

    public void put ( final String key, final String value )
    {
        this.modified = true;
        this.configurations.put ( key, value );
    }
}
