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
package org.eclipse.packagedrone.repo.aspect;

import org.eclipse.packagedrone.repo.Severity;

public interface ValidationContext
{
    /**
     * Add a validation message to the artifact currently being processed
     *
     * @param severity
     *            the severity of the message
     * @param message
     *            the message itself
     */
    public void validationMessage ( Severity severity, String message );

    /**
     * Add a validation information to the artifact currently being processed
     *
     * @param message
     *            the message of the information
     */
    public default void validationInformation ( final String message )
    {
        validationMessage ( Severity.INFO, message );
    }

    /**
     * Add a validation warning to the artifact currently being processed
     *
     * @param message
     *            the message of the warning
     */
    public default void validationWarning ( final String message )
    {
        validationMessage ( Severity.WARNING, message );
    }

    /**
     * Add a validation error to the artifact currently being processed
     *
     * @param message
     *            the message of the error
     */
    public default void validationError ( final String message )
    {
        validationMessage ( Severity.ERROR, message );
    }
}
