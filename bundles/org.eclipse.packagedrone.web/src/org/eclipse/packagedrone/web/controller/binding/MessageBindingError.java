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
package org.eclipse.packagedrone.web.controller.binding;

public class MessageBindingError implements BindingError
{

    private final String errorMessage;

    public MessageBindingError ( final String errorMessage )
    {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage ()
    {
        return this.errorMessage;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[MessageError: %s]", this.errorMessage );
    }
}
