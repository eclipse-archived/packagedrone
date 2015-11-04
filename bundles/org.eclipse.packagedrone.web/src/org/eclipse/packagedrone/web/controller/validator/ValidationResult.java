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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.web.controller.binding.BindingError;

public class ValidationResult
{
    public static final ValidationResult EMPTY = new ValidationResult ();

    private Map<String, List<BindingError>> errors = Collections.emptyMap ();

    private Set<String> markers = Collections.emptySet ();

    public void setMarkers ( final Set<String> markers )
    {
        this.markers = markers;
    }

    public Set<String> getMarkers ()
    {
        return this.markers;
    }

    public void setErrors ( final Map<String, List<BindingError>> errors )
    {
        this.errors = errors;
    }

    public Map<String, List<BindingError>> getErrors ()
    {
        return this.errors;
    }
}
