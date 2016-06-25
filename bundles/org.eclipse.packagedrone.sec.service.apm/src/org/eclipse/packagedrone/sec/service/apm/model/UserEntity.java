/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S2162
 *******************************************************************************/
package org.eclipse.packagedrone.sec.service.apm.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UserEntity
{
    private String id;

    private String name;

    private Date registrationDate;

    private String email;

    private String emailTokenSalt;

    private String emailToken;

    private Date emailTokenDate;

    private boolean emailVerified;

    private String passwordHash;

    private String passwordSalt;

    private boolean locked;

    private String rememberMeTokenHash;

    private String rememberMeTokenSalt;

    private Set<String> roles = new HashSet<> ();

    public UserEntity ()
    {
    }

    public UserEntity ( final UserEntity other )
    {
        this.id = other.id;
        this.name = other.name;
        this.registrationDate = other.registrationDate;
        this.email = other.email;
        this.emailTokenSalt = other.emailTokenSalt;
        this.emailToken = other.emailToken;
        this.emailTokenDate = other.emailTokenDate;
        this.emailVerified = other.emailVerified;
        this.passwordHash = other.passwordHash;
        this.passwordSalt = other.passwordSalt;
        this.locked = other.locked;
        this.rememberMeTokenHash = other.rememberMeTokenHash;
        this.rememberMeTokenSalt = other.rememberMeTokenSalt;
        this.roles = new HashSet<> ( other.roles );
    }

    public void setRoles ( final Set<String> roles )
    {
        this.roles = roles;
    }

    public Set<String> getRoles ()
    {
        return this.roles;
    }

    public void setRememberMeTokenHash ( final String rememberMeTokenHash )
    {
        this.rememberMeTokenHash = rememberMeTokenHash;
    }

    public String getRememberMeTokenHash ()
    {
        return this.rememberMeTokenHash;
    }

    public void setRememberMeTokenSalt ( final String rememberMeTokenSalt )
    {
        this.rememberMeTokenSalt = rememberMeTokenSalt;
    }

    public String getRememberMeTokenSalt ()
    {
        return this.rememberMeTokenSalt;
    }

    public void setLocked ( final boolean locked )
    {
        this.locked = locked;
    }

    public boolean isLocked ()
    {
        return this.locked;
    }

    public void setEmailVerified ( final boolean emailVerified )
    {
        this.emailVerified = emailVerified;
    }

    public boolean isEmailVerified ()
    {
        return this.emailVerified;
    }

    public void setPasswordSalt ( final String passwordSalt )
    {
        this.passwordSalt = passwordSalt;
    }

    public String getPasswordSalt ()
    {
        return this.passwordSalt;
    }

    public void setPasswordHash ( final String passwordHash )
    {
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash ()
    {
        return this.passwordHash;
    }

    public void setRegistrationDate ( final Date registrationDate )
    {
        this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate ()
    {
        return this.registrationDate;
    }

    public String getEmail ()
    {
        return this.email;
    }

    public void setEmail ( final String email )
    {
        this.email = email;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getEmailTokenSalt ()
    {
        return this.emailTokenSalt;
    }

    public void setEmailTokenSalt ( final String emailTokenSalt )
    {
        this.emailTokenSalt = emailTokenSalt;
    }

    public String getEmailToken ()
    {
        return this.emailToken;
    }

    public void setEmailToken ( final String emailToken )
    {
        this.emailToken = emailToken;
    }

    public Date getEmailTokenDate ()
    {
        return this.emailTokenDate;
    }

    public void setEmailTokenDate ( final Date emailTokenDate )
    {
        this.emailTokenDate = emailTokenDate;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( this.getClass() != obj.getClass() )
        {
            return false;
        }
        final UserEntity other = (UserEntity)obj;
        if ( this.id == null )
        {
            if ( other.id != null )
            {
                return false;
            }
        }
        else if ( !this.id.equals ( other.id ) )
        {
            return false;
        }
        return true;
    }

}
