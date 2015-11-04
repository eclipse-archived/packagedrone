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

public class SimpleBinding implements Binding
{
    private final Object value;

    private final BindingResult bindingResult;

    public SimpleBinding ( final Object value )
    {
        this.value = value;
        this.bindingResult = new SimpleBindingResult ();
    }

    public SimpleBinding ( final Object value, final BindingResult bindingResult )
    {
        this.value = value;
        this.bindingResult = bindingResult;
    }

    @Override
    public Object getValue ()
    {
        return this.value;
    }

    @Override
    public BindingResult getBindingResult ()
    {
        return this.bindingResult;
    }
}
