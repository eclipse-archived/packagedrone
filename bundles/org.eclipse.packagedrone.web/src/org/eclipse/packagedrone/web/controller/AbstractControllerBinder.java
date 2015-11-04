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
package org.eclipse.packagedrone.web.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.web.controller.binding.Binder;

public abstract class AbstractControllerBinder implements Binder, ControllerBinderParametersAware
{

    private Map<String, String> parameters = Collections.emptyMap ();

    protected Map<String, String> getParameters ()
    {
        return this.parameters;
    }

    @Override
    public void setParameters ( final ControllerBinderParameter[] parameters )
    {
        if ( parameters == null || parameters.length <= 0 )
        {
            return;
        }

        final Map<String, String> map = new HashMap<> ( parameters.length );
        for ( final ControllerBinderParameter param : parameters )
        {
            map.put ( param.key (), param.value () );
        }

        this.parameters = map;
    }
}
