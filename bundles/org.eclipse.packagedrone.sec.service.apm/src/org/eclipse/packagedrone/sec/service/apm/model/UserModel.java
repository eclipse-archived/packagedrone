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
package org.eclipse.packagedrone.sec.service.apm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.packagedrone.sec.DatabaseUserInformation;
import org.eclipse.packagedrone.sec.service.apm.Helper;

public class UserModel
{
    private final Map<String, DatabaseUserInformation> users;

    private final Map<String, DatabaseUserInformation> usersByEmail;

    private final List<DatabaseUserInformation> allUsers;

    public UserModel ( final Collection<UserEntity> users )
    {
        this.users = new HashMap<> ( users.size () );
        this.usersByEmail = new HashMap<> ( users.size () );

        final ArrayList<DatabaseUserInformation> allUsers = new ArrayList<> ( users.size () );

        for ( final UserEntity user : users )
        {
            final DatabaseUserInformation dui = new DatabaseUserInformation ( user.getId (), null, user.getRoles (), Helper.toDetails ( user ) );

            this.users.put ( user.getId (), dui );
            final String email = user.getEmail ();
            if ( email != null )
            {
                this.usersByEmail.put ( email, dui );
            }
            allUsers.add ( dui );
        }

        this.allUsers = Collections.unmodifiableList ( allUsers );
    }

    public Optional<DatabaseUserInformation> getUser ( final String userId )
    {
        return Optional.ofNullable ( this.users.get ( userId ) );
    }

    public Optional<DatabaseUserInformation> findUserByEmail ( final String email )
    {
        return Optional.ofNullable ( this.usersByEmail.get ( email ) );
    }

    public List<DatabaseUserInformation> listAll ()
    {
        return this.allUsers;
    }

    public boolean isEmpty ()
    {
        return this.users.isEmpty ();
    }
}
