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
package org.eclipse.packagedrone.utils;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class Exceptions
{

    @FunctionalInterface
    public interface ThrowingRunnable
    {
        public void run () throws Exception;
    }

    /**
     * Call and wrap {@link Exception}s into a {@link RuntimeException}
     *
     * @param callable
     *            the {@link Callable} to call
     * @return the return value of the callable
     */
    public static <T> T wrapException ( final Callable<T> callable )
    {
        try
        {
            return callable.call ();
        }
        catch ( final RuntimeException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public static void wrapException ( final ThrowingRunnable run )
    {
        wrapException ( () -> {
            run.run ();
            return null;
        } );
    }

    /**
     * Call and wrap {@link Exception}s into an exception provided by the
     * function.
     * <p>
     * If the function returns {@code null}, a {@link RuntimeException} will be
     * created instead.
     * </p>
     *
     * @param callable
     *            the {@link Callable} to call
     * @return the return value of the callable
     */
    public static <T> T wrapException ( final Callable<T> callable, final Function<Exception, RuntimeException> func )
    {
        try
        {
            return callable.call ();
        }
        catch ( final RuntimeException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            RuntimeException t = func.apply ( e );
            if ( t == null )
            {
                t = new RuntimeException ( e );
            }
            else
            {
                // fixing the stack trace to be a bit more precise
                t.setStackTrace ( Thread.currentThread ().getStackTrace () );
            }

            throw t;
        }
    }

    public static void wrapException ( final ThrowingRunnable run, final Function<Exception, RuntimeException> func )
    {
        wrapException ( () -> {
            run.run ();
            return null;
        } , func );
    }
}
