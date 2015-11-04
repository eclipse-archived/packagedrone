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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.web.controller.binding.BindingError;

public class CompositeValidator implements Validator
{
    private final List<Validator> validators;

    public CompositeValidator ( final Validator... validators )
    {
        this.validators = Arrays.asList ( validators );
    }

    public CompositeValidator ( final List<Validator> validators )
    {
        this.validators = validators;
    }

    @Override
    public ValidationResult validate ( final Object target )
    {
        final Map<String, List<BindingError>> compositeResult = new HashMap<> ();
        final Set<String> markers = new HashSet<> ();

        for ( final Validator validator : this.validators )
        {
            final ValidationResult result = validator.validate ( target );

            if ( result == null )
            {
                continue;
            }

            markers.addAll ( result.getMarkers () );

            for ( final Map.Entry<String, List<BindingError>> entry : result.getErrors ().entrySet () )
            {
                List<BindingError> pos = compositeResult.get ( entry.getKey () );
                if ( pos == null )
                {
                    pos = new LinkedList<> ();
                    compositeResult.put ( entry.getKey (), pos );
                }
                pos.addAll ( entry.getValue () );
            }
        }

        final ValidationResult validationResult = new ValidationResult ();
        validationResult.setErrors ( compositeResult );
        validationResult.setMarkers ( markers );
        return validationResult;
    }
}
