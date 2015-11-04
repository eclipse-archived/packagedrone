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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.deploy.DeployKey;

public interface ChannelServiceAccess
{
    public String mapToId ( String name );

    public String mapToName ( String id );

    public Map<String, String> getNameMap ();

    public List<DeployGroup> getDeployGroups ();

    public DeployGroup getDeployGroup ( String id );

    public DeployKey getDeployKey ( String keyId );

    /**
     * Get the map of channel ids to deploy groups
     *
     * @return the channel to deploy group map
     */
    public Map<String, Set<String>> getDeployGroupMap ();
}
