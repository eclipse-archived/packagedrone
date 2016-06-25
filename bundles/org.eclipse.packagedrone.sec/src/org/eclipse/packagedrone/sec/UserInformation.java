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
package org.eclipse.packagedrone.sec;

import java.util.Collections;
import java.util.Set;

public class UserInformation implements Comparable<UserInformation>
{
    private final String id;

    private final Set<String> roles;

    private final String rememberMeToken;

    public UserInformation ( final String id, final String rememberMeToken, final Set<String> roles )
    {
        this.id = id;
        this.rememberMeToken = rememberMeToken;
        this.roles = roles == null ? Collections.<String> emptySet () : Collections.unmodifiableSet ( roles );
    }

    public UserInformation ( final String id, final Set<String> roles )
    {
        this ( id, null, roles );
    }

    public String getId ()
    {
        return this.id;
    }

    public String getRememberMeToken ()
    {
        return this.rememberMeToken;
    }

    public Set<String> getRoles ()
    {
        return this.roles;
    }

    public <T> T getDetails ( final Class<T> detailsClazz )
    {
        return null;
    }

    /**
     * This is called by JSP pages to get the default details object
     *
     * @return the default details object, or <code>null</code> if none exists
     */
    public Object getDetails ()
    {
        return getDetails ( Object.class );
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
        final UserInformation other = (UserInformation)obj;
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

    @Override
    public int compareTo ( final UserInformation o )
    {
        return this.id.compareTo ( o.id );
    }

}
