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
package org.eclipse.packagedrone.web.controller.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.packagedrone.web.controller.binding.ExceptionError;
import org.eclipse.packagedrone.web.controller.form.DataValidator;

public class FormDataValidator implements Validator
{
    @Override
    public ValidationResult validate ( final Object target )
    {
        final SimpleValidationContext ctx = new SimpleValidationContext ();

        for ( final Method m : target.getClass ().getMethods () )
        {
            final DataValidator dv = m.getAnnotation ( DataValidator.class );
            if ( dv == null )
            {
                continue;
            }

            try
            {
                m.invoke ( target, ctx );
            }
            catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
            {
                ctx.error ( null, new ExceptionError ( e ) );
            }
        }

        return ctx.getResult ();
    }
}
