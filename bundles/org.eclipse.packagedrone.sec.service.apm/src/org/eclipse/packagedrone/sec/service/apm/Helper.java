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
package org.eclipse.packagedrone.sec.service.apm;

import java.util.HashSet;

import org.eclipse.packagedrone.sec.DatabaseDetails;
import org.eclipse.packagedrone.sec.DatabaseDetailsBean;
import org.eclipse.packagedrone.sec.service.apm.model.UserEntity;

public final class Helper
{
    private Helper ()
    {
    }

    public static DatabaseDetails toDetails ( final UserEntity user )
    {
        if ( user == null )
        {
            return null;
        }

        final DatabaseDetailsBean bean = new DatabaseDetailsBean ();

        bean.setDeleted ( false );
        bean.setEmail ( user.getEmail () );
        bean.setEmailVerified ( user.isEmailVerified () );
        bean.setLocked ( user.isLocked () );
        bean.setName ( user.getName () );
        bean.setRegistrationDate ( user.getRegistrationDate () );
        bean.setRoles ( new HashSet<> ( user.getRoles () ) );

        return new DatabaseDetails ( bean );
    }
}
