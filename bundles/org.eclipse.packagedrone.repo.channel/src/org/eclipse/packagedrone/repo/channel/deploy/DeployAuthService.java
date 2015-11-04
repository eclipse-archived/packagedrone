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
package org.eclipse.packagedrone.repo.channel.deploy;

import java.util.List;

public interface DeployAuthService
{
    public List<DeployGroup> listGroups ( int position, int count );

    public DeployGroup createGroup ( String name );

    public void deleteGroup ( String groupId );

    public void updateGroup ( String groupId, String name );

    public DeployGroup getGroup ( String groupId );

    public DeployKey createDeployKey ( String groupId, String name );

    public DeployKey deleteDeployKey ( String keyId );

    public DeployKey getDeployKey ( String keyId );

    public DeployKey updateDeployKey ( String keyId, String name );
}
