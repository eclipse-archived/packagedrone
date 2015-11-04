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
package org.eclipse.packagedrone.repo.adapter.maven.upload;

/**
 * Thrown when a checksum validation is detected
 */
public class ChecksumValidationException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ChecksumValidationException ()
    {
        super ();
    }

    public ChecksumValidationException ( final String message, final Throwable cause )
    {
        super ( message, cause );
    }

    public ChecksumValidationException ( final String message )
    {
        super ( message );
    }

    public ChecksumValidationException ( final Throwable cause )
    {
        super ( cause );
    }

}
