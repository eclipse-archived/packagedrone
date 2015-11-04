/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller.validator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;

import org.eclipse.packagedrone.utils.validation.ValidationBundle;
import org.eclipse.packagedrone.web.controller.binding.BindingError;

public class JavaValidator implements Validator
{
    public static class ConstraintViolationBindingError implements BindingError
    {
        private final ConstraintViolation<?> violation;

        public ConstraintViolationBindingError ( final ConstraintViolation<?> violation )
        {
            this.violation = violation;
        }

        @Override
        public String getMessage ()
        {
            return this.violation.getMessage ();
        }
    }

    @Override
    public ValidationResult validate ( final Object target )
    {
        final Set<ConstraintViolation<Object>> vr = ValidationBundle.getValidator ().validate ( target );

        if ( vr == null || vr.isEmpty () )
        {
            return ValidationResult.EMPTY;
        }

        final Map<String, List<BindingError>> result = new HashMap<> ();

        for ( final ConstraintViolation<Object> entry : vr )
        {
            final String path = makePath ( entry );
            List<BindingError> errors = result.get ( path );
            if ( errors == null )
            {
                errors = new LinkedList<> ();
                result.put ( path, errors );
            }
            errors.add ( convert ( entry ) );
        }

        final ValidationResult validationResult = new ValidationResult ();
        validationResult.setErrors ( result );
        return validationResult;
    }

    private BindingError convert ( final ConstraintViolation<Object> entry )
    {
        return new ConstraintViolationBindingError ( entry );
    }

    private String makePath ( final ConstraintViolation<Object> entry )
    {
        final StringBuilder sb = new StringBuilder ();

        final Path p = entry.getPropertyPath ();
        for ( final Node n : p )
        {
            if ( sb.length () > 0 )
            {
                sb.append ( '.' );
            }
            sb.append ( n.getName () );
        }

        return sb.toString ();
    }
}
