/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.converter;

public class ConversionException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public ConversionException ()
    {
        super ();
    }

    public ConversionException ( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace )
    {
        super ( message, cause, enableSuppression, writableStackTrace );
    }

    public ConversionException ( final String message, final Throwable cause )
    {
        super ( message, cause );
    }

    public ConversionException ( final String message )
    {
        super ( message );
    }

    public ConversionException ( final Throwable cause )
    {
        super ( cause );
    }

}
