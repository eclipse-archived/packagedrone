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

import org.eclipse.packagedrone.utils.converter.ConverterManager;

/**
 * A Binder which simply adds an error binding to the root
 *
 * @see Binding#errorBinding(Throwable)
 */
public class ErrorBinder implements Binder
{
    private final Throwable error;

    public ErrorBinder ( final Throwable e )
    {
        this.error = e;
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        return Binding.errorBinding ( this.error );
    }
}
