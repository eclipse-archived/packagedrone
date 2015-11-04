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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.web.controller.binding.BindingError;
import org.eclipse.packagedrone.web.controller.binding.MessageBindingError;

public class SimpleValidationContext implements ValidationContext
{

    private final Map<String, List<BindingError>> result = new HashMap<> ();

    private final Set<String> markers = new HashSet<> ();

    public Set<String> getMarkers ()
    {
        return this.markers;
    }

    @Override
    public void error ( String path, final BindingError error )
    {
        if ( path == null )
        {
            path = "";
        }

        List<BindingError> pos = this.result.get ( path );
        if ( pos == null )
        {
            pos = new LinkedList<> ();
            this.result.put ( path, pos );
        }
        pos.add ( error );
    }

    @Override
    public void error ( final BindingError error )
    {
        error ( null, error );
    }

    @Override
    public void error ( final String errorMessage )
    {
        error ( null, errorMessage );
    }

    @Override
    public void error ( final String path, final String errorMessage )
    {
        error ( path, new MessageBindingError ( errorMessage ) );
    }

    public ValidationResult getResult ()
    {
        final ValidationResult vr = new ValidationResult ();
        vr.setErrors ( this.result );
        vr.setMarkers ( this.markers );
        return vr;
    }

    @Override
    public void setMarker ( final String marker )
    {
        this.markers.add ( marker );
    }
}
