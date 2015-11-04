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
package org.eclipse.packagedrone.sec.service;

public class LoginException extends Exception
{
    private static final long serialVersionUID = 1L;

    private final String details;

    public LoginException ( final String message, final String details, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace )
    {
        super ( message, cause, enableSuppression, writableStackTrace );
        this.details = details;
    }

    public LoginException ( final String message, final String details, final Throwable cause )
    {
        super ( message, cause );
        this.details = details;
    }

    public LoginException ( final String message, final String details )
    {
        super ( message );
        this.details = details;
    }

    public LoginException ( final String message )
    {
        super ( message );
        this.details = null;
    }

    public String getDetails ()
    {
        return this.details;
    }

}
