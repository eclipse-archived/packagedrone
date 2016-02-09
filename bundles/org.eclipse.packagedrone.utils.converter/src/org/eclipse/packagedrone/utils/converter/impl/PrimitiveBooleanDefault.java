/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.converter.impl;

import org.eclipse.packagedrone.utils.converter.DefaultProvider;

public class PrimitiveBooleanDefault implements DefaultProvider
{
    public static final DefaultProvider INSTANCE = new PrimitiveBooleanDefault ();

    @Override
    public boolean providesFor ( final Class<?> clazz )
    {
        return clazz.isAssignableFrom ( boolean.class );
    }

    @Override
    public Object defaultValue ()
    {
        return false;
    }

}
