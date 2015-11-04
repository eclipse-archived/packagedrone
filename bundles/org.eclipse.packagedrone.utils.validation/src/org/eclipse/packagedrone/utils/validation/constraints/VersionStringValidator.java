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
package org.eclipse.packagedrone.utils.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.osgi.framework.Version;

public class VersionStringValidator implements ConstraintValidator<VersionString, String>
{
    @Override
    public void initialize ( final VersionString constraint )
    {
    }

    @Override
    public boolean isValid ( final String value, final ConstraintValidatorContext context )
    {
        if ( value == null )
        {
            return true;
        }
        if ( value.isEmpty () )
        {
            return true;
        }

        try
        {
            Version.parseVersion ( value );
            return true;
        }
        catch ( final Exception e )
        {
            return false;
        }
    }

}
