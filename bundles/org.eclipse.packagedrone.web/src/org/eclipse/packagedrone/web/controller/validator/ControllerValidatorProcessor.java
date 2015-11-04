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

public class ControllerValidatorProcessor implements Validator
{

    private final Object controller;

    public ControllerValidatorProcessor ( final Object controller )
    {
        this.controller = controller;
    }

    @Override
    public ValidationResult validate ( final Object target )
    {
        if ( target == null )
        {
            return null;
        }

        final SimpleValidationContext ctx = new SimpleValidationContext ();

        for ( final Method m : this.controller.getClass ().getMethods () )
        {
            final ControllerValidator cv = m.getAnnotation ( ControllerValidator.class );
            if ( cv == null )
            {
                continue;
            }

            if ( cv.formDataClass () == null )
            {
                continue;
            }

            if ( !cv.formDataClass ().isAssignableFrom ( target.getClass () ) )
            {
                continue;
            }

            try
            {
                m.invoke ( this.controller, target, ctx );
            }
            catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
            {
                ctx.error ( null, new ExceptionError ( e ) );
            }
        }

        return ctx.getResult ();
    }
}
