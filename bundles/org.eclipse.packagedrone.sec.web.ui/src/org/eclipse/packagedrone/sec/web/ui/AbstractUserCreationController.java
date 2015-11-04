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

import org.eclipse.packagedrone.sec.CreateUser;
import org.eclipse.packagedrone.sec.DatabaseUserInformation;
import org.eclipse.packagedrone.sec.UserStorage;
import org.eclipse.packagedrone.sec.service.password.BadPasswordException;
import org.eclipse.packagedrone.sec.service.password.PasswordChecker;
import org.eclipse.packagedrone.web.controller.binding.MessageBindingError;
import org.eclipse.packagedrone.web.controller.validator.ControllerValidator;
import org.eclipse.packagedrone.web.controller.validator.ValidationContext;

public class AbstractUserCreationController
{
    protected UserStorage storage;

    protected PasswordChecker passwordChecker;

    public void setStorage ( final UserStorage storage )
    {
        this.storage = storage;
    }

    public void setPasswordChecker ( final PasswordChecker passwordChecker )
    {
        this.passwordChecker = passwordChecker;
    }

    @ControllerValidator ( formDataClass = CreateUser.class )
    public void validateCreateUser ( final CreateUser createUser, final ValidationContext context )
    {
        final DatabaseUserInformation user = this.storage.getUserDetailsByEmail ( createUser.getEmail () );
        if ( user != null )
        {
            context.error ( "email", "A user is already registered for this e-mail address" );
            context.setMarker ( "duplicateEmail" );
        }

        if ( createUser.getPassword () != null && !createUser.getPassword ().isEmpty () )
        {
            checkPassword ( createUser.getPassword (), context );
        }
    }

    @ControllerValidator ( formDataClass = NewPassword.class )
    public void validatePassword ( final NewPassword data, final ValidationContext context )
    {
        checkPassword ( data.getPassword (), context );
    }

    protected void checkPassword ( final String password, final ValidationContext context )
    {
        try
        {
            this.passwordChecker.checkPassword ( password );
        }
        catch ( final BadPasswordException e )
        {
            context.error ( "password", new MessageBindingError ( e.getMessage () ) );
        }
    }

}
