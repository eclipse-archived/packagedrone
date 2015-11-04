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
package org.eclipse.packagedrone.repo.aspect.aggregate;

import java.util.Set;

import org.eclipse.packagedrone.repo.Severity;

public interface AggregationValidationContext
{
    public void validationMessage ( Severity severity, String message, Set<String> artifactIds );

    public default void validationInformation ( final String message, final Set<String> artifactIds )
    {
        validationMessage ( Severity.INFO, message, artifactIds );
    }

    public default void validationWarning ( final String message, final Set<String> artifactIds )
    {
        validationMessage ( Severity.WARNING, message, artifactIds );
    }

    public default void validationError ( final String message, final Set<String> artifactIds )
    {
        validationMessage ( Severity.ERROR, message, artifactIds );
    }

}
