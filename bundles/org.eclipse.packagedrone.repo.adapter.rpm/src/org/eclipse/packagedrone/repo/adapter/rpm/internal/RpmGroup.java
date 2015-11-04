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
package org.eclipse.packagedrone.repo.adapter.rpm.internal;

import org.eclipse.packagedrone.repo.adapter.rpm.Constants;
import org.eclipse.packagedrone.repo.aspect.group.Group;
import org.eclipse.packagedrone.repo.aspect.group.GroupInformation;

public class RpmGroup implements Group
{
    private static final GroupInformation INFO = new GroupInformation () {

        @Override
        public String getName ()
        {
            return "RPM";
        }

        @Override
        public String getId ()
        {
            return Constants.GROUP_ID;
        }
    };

    @Override
    public GroupInformation getInformation ()
    {
        return INFO;
    }

}
