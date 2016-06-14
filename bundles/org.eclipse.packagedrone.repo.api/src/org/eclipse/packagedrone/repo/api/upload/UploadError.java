/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.api.upload;

public class UploadError
{
    private String message;

    public UploadError ( final String message )
    {
        this.message = message;
    }

    public UploadError ()
    {
    }

    public String getMessage ()
    {
        return this.message;
    }

    public void setMessage ( final String message )
    {
        this.message = message;
    }
}
