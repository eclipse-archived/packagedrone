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
package org.eclipse.packagedrone.sec.web.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

public class UserDetailsBean
{
    private String name;

    @Email
    @NotEmpty
    private String email;

    private Set<String> roles = new HashSet<> ();

    public UserDetailsBean ()
    {
    }

    public UserDetailsBean ( final UserDetailsBean other )
    {
        this.name = other.name;
        this.email = other.email;
        this.roles = new HashSet<> ( other.roles == null ? Collections.<String> emptyList () : other.roles );
    }

    public void setRoles ( final Set<String> roles )
    {
        this.roles = roles;
    }

    public Set<String> getRoles ()
    {
        return this.roles;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getEmail ()
    {
        return this.email;
    }

    public void setEmail ( final String email )
    {
        this.email = email;
    }
}
