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
package org.eclipse.packagedrone.repo.channel.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Disposing<T> implements AutoCloseable, InvocationHandler
{
    private final T target;

    private final T proxy;

    private volatile boolean disposed;

    public Disposing ( final Class<T> clazz, final T target )
    {
        if ( target == null )
        {
            throw new NullPointerException ( "'target' must not be null" );
        }

        this.target = target;
        this.proxy = clazz.cast ( Proxy.newProxyInstance ( clazz.getClassLoader (), new Class<?>[] { clazz }, this ) );
    }

    public T getTarget ()
    {
        return this.proxy;
    }

    @Override
    public void close ()
    {
        this.disposed = true;
    }

    @Override
    public Object invoke ( final Object proxy, final Method method, final Object[] args ) throws Throwable
    {
        if ( this.disposed )
        {
            throw new IllegalStateException ( "Object is already disposed" );
        }
        return method.invoke ( this.target, args );
    }

    public static <T> Disposing<T> proxy ( final Class<T> clazz, final T target )
    {
        return new Disposing<T> ( clazz, target );
    }
}
