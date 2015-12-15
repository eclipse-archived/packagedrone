/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.impl.model.ChannelConfiguration;

public class ChannelServiceModel
{
    // FIXME: need to support old format

    private final Map<String, List<String>> nameMap;

    private final List<DeployGroup> deployGroups;

    private final Map<String, ChannelConfiguration> channels;

    /**
     * Map of channel ids to deploy groups
     */
    private final Map<String, Set<String>> deployGroupMap;

    public ChannelServiceModel ()
    {
        this.nameMap = new HashMap<> ();
        this.deployGroups = new CopyOnWriteArrayList<> ();
        this.deployGroupMap = new HashMap<> ();
        this.channels = new HashMap<> ();
    }

    public ChannelServiceModel ( final ChannelServiceModel other )
    {
        this.nameMap = new HashMap<> ( other.nameMap );
        this.deployGroups = new CopyOnWriteArrayList<> ( other.deployGroups );
        this.deployGroupMap = new HashMap<> ( other.deployGroupMap );
        this.channels = new HashMap<> ( other.channels );
    }

    public Map<String, List<String>> getNameMap ()
    {
        return this.nameMap;
    }

    public List<DeployGroup> getDeployGroups ()
    {
        return this.deployGroups;
    }

    public Map<String, Set<String>> getDeployGroupMap ()
    {
        return this.deployGroupMap;
    }

    public Map<String, ChannelConfiguration> getChannels ()
    {
        return this.channels;
    }
}
