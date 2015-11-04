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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserWriteModel
{
    private final Map<String, UserEntity> userMap;

    private final Map<String, UserEntity> userMailMap;

    private boolean changed;

    public UserWriteModel ()
    {
        this.userMap = new HashMap<> ();
        this.userMailMap = new HashMap<> ();
    }

    public UserWriteModel ( final Collection<UserEntity> users, final boolean clone )
    {
        this.userMap = new HashMap<> ( users.size () );
        this.userMailMap = new HashMap<> ( users.size () );

        for ( final UserEntity user : users )
        {
            final UserEntity newUser = clone ? new UserEntity ( user ) : user;
            this.userMap.put ( user.getId (), newUser );

            if ( newUser.getEmail () != null )
            {
                this.userMailMap.put ( newUser.getEmail (), newUser );
            }
        }
    }

    public UserWriteModel ( final UserWriteModel other )
    {
        this ( other.userMap.values (), true );
    }

    public boolean isChanged ()
    {
        return this.changed;
    }

    public Collection<UserEntity> asCollection ()
    {
        return Collections.unmodifiableCollection ( this.userMap.values () );
    }

    public Map<String, UserEntity> getAll ()
    {
        return Collections.unmodifiableMap ( this.userMap );
    }

    public Optional<UserEntity> getUser ( final String userId )
    {
        return Optional.ofNullable ( this.userMap.get ( userId ) );
    }

    public Optional<UserEntity> findByEmail ( final String email )
    {
        return Optional.ofNullable ( this.userMailMap.get ( email ) );
    }

    public UserEntity getCheckedUser ( final String userId )
    {
        return getUser ( userId ).orElseThrow ( () -> new UserNotFoundException () );
    }

    public void putUser ( final UserEntity user )
    {
        if ( user.getId () == null )
        {
            throw new IllegalArgumentException ( "User has no ID assigned" );
        }

        this.changed = true;

        this.userMap.put ( user.getId (), user );
        if ( user.getEmail () != null )
        {
            this.userMailMap.put ( user.getEmail (), user );
        }
    }

    public boolean removeUser ( final String userId )
    {
        this.changed = true;

        return this.userMap.remove ( userId ) != null;
    }
}
