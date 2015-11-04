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

import java.util.Set;

public class DatabaseUserInformation extends UserInformation
{
    private final DatabaseDetails details;

    public DatabaseUserInformation ( final String id, final String rememberMeToken, final Set<String> roles, final DatabaseDetails details )
    {
        super ( id, rememberMeToken, roles );
        this.details = details;
    }

    @Override
    public <T> T getDetails ( final Class<T> detailsClazz )
    {
        if ( detailsClazz == null )
        {
            return null;
        }

        if ( detailsClazz.isAssignableFrom ( DatabaseDetails.class ) )
        {
            return detailsClazz.cast ( this.details );
        }

        return null;
    }

}
