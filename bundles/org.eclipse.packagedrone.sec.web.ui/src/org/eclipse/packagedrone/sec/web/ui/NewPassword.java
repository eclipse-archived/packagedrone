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

import org.eclipse.packagedrone.web.controller.form.DataValidator;
import org.eclipse.packagedrone.web.controller.validator.ValidationContext;
import org.hibernate.validator.constraints.NotEmpty;

public class NewPassword
{
    private String email;

    private String token;

    @NotEmpty
    private String password;

    @NotEmpty
    private String passwordRepeat;

    private String currentPassword;

    public void setToken ( final String token )
    {
        this.token = token;
    }

    public String getToken ()
    {
        return this.token;
    }

    public void setEmail ( final String email )
    {
        this.email = email;
    }

    public String getEmail ()
    {
        return this.email;
    }

    public void setPassword ( final String password )
    {
        this.password = password;
    }

    public String getPassword ()
    {
        return this.password;
    }

    public void setPasswordRepeat ( final String passwordRepeat )
    {
        this.passwordRepeat = passwordRepeat;
    }

    public String getPasswordRepeat ()
    {
        return this.passwordRepeat;
    }

    public void setCurrentPassword ( final String currentPassword )
    {
        this.currentPassword = currentPassword;
    }

    public String getCurrentPassword ()
    {
        return this.currentPassword;
    }

    @DataValidator
    public void validatePasswords ( final ValidationContext context )
    {
        if ( ( this.password == null || this.password.isEmpty () ) && ( this.passwordRepeat == null || this.passwordRepeat.isEmpty () ) )
        {
            return;
        }

        if ( this.password == null || this.password.isEmpty () || !this.password.equals ( this.passwordRepeat ) )
        {
            context.error ( "passwordRepeat", "Passwords don't match" );
        }
    }
}
