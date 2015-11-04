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
package org.eclipse.packagedrone.repo.channel;

import java.util.Set;

import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;

public interface DeployKeysChannelAdapter
{
    public Set<DeployGroup> getDeployGroups ();

    public void assignDeployGroup ( String groupId );

    public void unassignDeployGroup ( String groupId );
}
