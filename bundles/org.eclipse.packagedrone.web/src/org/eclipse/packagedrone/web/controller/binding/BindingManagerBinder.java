/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller.binding;

import java.util.Map;

import org.eclipse.packagedrone.utils.converter.ConverterManager;

public class BindingManagerBinder extends MapBinder
{
    private boolean added;

    public BindingManagerBinder ()
    {
        super ();
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        if ( !this.added )
        {
            final Map<String, Object> data = getObjects ();
            data.put ( "bindingManager", bindingManager );
            data.put ( "bindingResult", bindingManager.getResult () );
            this.added = true;
        }

        return super.performBind ( target, converter, bindingManager );
    }

}
