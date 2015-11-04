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
package org.eclipse.packagedrone.job;

import org.eclipse.scada.utils.ExceptionHelper;

public class ErrorInformation
{
    private final String message;

    private final String formatted;

    private final String rootFormatted;

    ErrorInformation ( final String message, final String formatted, final String rootFormatted )
    {
        this.message = message;
        this.formatted = formatted;
        this.rootFormatted = rootFormatted;
    }

    public String getMessage ()
    {
        return this.message;
    }

    public String getFormatted ()
    {
        return this.formatted;
    }

    public String getRootFormatted ()
    {
        return this.rootFormatted;
    }

    public static ErrorInformation createFrom ( final Throwable e )
    {
        if ( e == null )
        {
            return null;
        }

        return new ErrorInformation ( ExceptionHelper.getMessage ( e ), ExceptionHelper.formatted ( e ), ExceptionHelper.formatted ( ExceptionHelper.getRootCause ( e ) ) );

    }

}
