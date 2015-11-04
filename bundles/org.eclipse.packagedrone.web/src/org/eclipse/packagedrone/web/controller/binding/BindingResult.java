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
package org.eclipse.packagedrone.web.controller.binding;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BindingResult
{
    public static final String ATTRIBUTE_NAME = BindingResult.class.getName ();

    public boolean hasErrors ();

    public BindingResult getChild ( String name );

    public BindingResult getChildOrAdd ( String name );

    public Map<String, BindingResult> getChildren ();

    public void addChild ( String name, BindingResult bindingResult );

    public void addError ( BindingError error );

    public void addErrors ( Collection<BindingError> error );

    /**
     * Get a list of all errors, including child errors. <br>
     * See also {@link #getLocalErrors()} which does not return child errors
     */
    public List<BindingError> getErrors ();

    /**
     * Get an unmodifiable list of local error
     */
    public List<BindingError> getLocalErrors ();

    public boolean hasMarker ( String marker );
}
