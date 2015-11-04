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
package org.eclipse.packagedrone.utils.profiler.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.eclipse.packagedrone.utils.profiler.Profile;

public class ProfilerInvocationHandler implements InvocationHandler
{
    private final Object service;

    public ProfilerInvocationHandler ( final Object service )
    {
        this.service = service;
    }

    @Override
    public Object invoke ( final Object proxy, final Method method, final Object[] args ) throws Throwable
    {
        return Profile.call ( makeName ( method ), () -> method.invoke ( this.service, args ) );
    }

    private String makeName ( final Method method )
    {
        return this.service.getClass ().getName () + "." + method.getName ();
    }

}
