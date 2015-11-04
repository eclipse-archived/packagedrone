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

public class ChannelServiceModel
{
    private final Map<String, String> nameMap;

    private final List<DeployGroup> deployGroups;

    /**
     * Map of channel ids to deploy groups
     */
    private final Map<String, Set<String>> deployGroupMap;

    public ChannelServiceModel ()
    {
        this.nameMap = new HashMap<> ();
        this.deployGroups = new CopyOnWriteArrayList<> ();
        this.deployGroupMap = new HashMap<> ();
    }

    public ChannelServiceModel ( final ChannelServiceModel other )
    {
        this.nameMap = new HashMap<> ( other.nameMap );
        this.deployGroups = new CopyOnWriteArrayList<> ( other.deployGroups );
        this.deployGroupMap = new HashMap<> ( other.deployGroupMap );
    }

    public Map<String, String> getNameMap ()
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
}
