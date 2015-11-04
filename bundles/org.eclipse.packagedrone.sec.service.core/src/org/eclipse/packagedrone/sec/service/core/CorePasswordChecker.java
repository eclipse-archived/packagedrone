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
package org.eclipse.packagedrone.sec.service.core;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.manage.core.CoreHelper;
import org.eclipse.packagedrone.repo.manage.core.CoreService;
import org.eclipse.packagedrone.sec.service.password.BadPasswordException;
import org.eclipse.packagedrone.sec.service.password.PasswordChecker;

public class CorePasswordChecker implements PasswordChecker
{
    private static final MetaKey KEY_MIN_PASSWORD_LENGTH = new MetaKey ( "core", "min-password-length" );

    private static final int DEFAULT_MIN_LENGTH = 6;

    private CoreService service;

    public void setService ( final CoreService service )
    {
        this.service = service;
    }

    @Override
    public void checkPassword ( final String password ) throws BadPasswordException
    {
        int minLength = CoreHelper.getInteger ( this.service, KEY_MIN_PASSWORD_LENGTH, DEFAULT_MIN_LENGTH );
        if ( minLength <= 0 )
        {
            minLength = 6;
        }

        checkPassword ( password, minLength );
    }

    private void checkPassword ( final String password, final int minLength ) throws BadPasswordException
    {
        if ( password == null )
        {
            throw new BadPasswordException ( "Password must not be empty" );
        }

        if ( password.length () < minLength )
        {
            throw new BadPasswordException ( String.format ( "Password  must be at least %s characters long", minLength ) );
        }
    }

}
