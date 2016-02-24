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
package org.eclipse.packagedrone.utils.deb;

public class ParserException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ParserException ()
    {
        super ();
    }

    public ParserException ( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace )
    {
        super ( message, cause, enableSuppression, writableStackTrace );
    }

    public ParserException ( final String message, final Throwable cause )
    {
        super ( message, cause );
    }

    public ParserException ( final String message )
    {
        super ( message );
    }

    public ParserException ( final Throwable cause )
    {
        super ( cause );
    }

}
