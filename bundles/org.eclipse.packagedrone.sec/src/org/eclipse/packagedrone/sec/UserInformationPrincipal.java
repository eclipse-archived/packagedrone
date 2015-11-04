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
package org.eclipse.packagedrone.sec;

import java.security.Principal;

public class UserInformationPrincipal implements Principal
{

    private final UserInformation userInformation;

    public UserInformationPrincipal ( final UserInformation userInformation )
    {
        this.userInformation = userInformation;
    }

    @Override
    public String getName ()
    {
        return this.userInformation.getId ();
    }

    public UserInformation getUserInformation ()
    {
        return this.userInformation;
    }
}
