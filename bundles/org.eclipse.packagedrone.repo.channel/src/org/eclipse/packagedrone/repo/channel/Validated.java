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
package org.eclipse.packagedrone.repo.channel;

import java.util.Collection;

import org.eclipse.packagedrone.repo.Severity;

public interface Validated
{
    public Collection<ValidationMessage> getValidationMessages ();

    public default long getValidationErrorCount ()
    {
        return getValidationMessages ().stream ().filter ( msg -> Severity.ERROR == msg.getSeverity () ).count ();
    }

    public default long getValidationWarningCount ()
    {
        return getValidationMessages ().stream ().filter ( msg -> Severity.WARNING == msg.getSeverity () ).count ();
    }

    /**
     * Get the overall validation state
     * <p>
     * This method will return {@link Severity#ERROR} if there are errors
     * present. If not then it will return {@link Severity#WARNING} if there are
     * warnings present. If not, then it will return <code>null</code>.
     * </p>
     *
     * @return the calculated overall validation state
     */
    public default Severity getOverallValidationState ()
    {
        if ( getValidationErrorCount () > 0 )
        {
            return Severity.ERROR;
        }
        else if ( getValidationWarningCount () > 0 )
        {
            return Severity.WARNING;
        }
        return null;
    }
}
