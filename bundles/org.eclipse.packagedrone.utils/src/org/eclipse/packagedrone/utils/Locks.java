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
package org.eclipse.packagedrone.utils;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * Helper class for using {@link Lock}s
 */
public final class Locks
{
    private Locks ()
    {
    }

    public interface Locked extends AutoCloseable
    {
        @Override
        public void close ();
    }

    /**
     * Lock and return {@link AutoCloseable} instance for unlocking
     * Use with a <tt>try-with-resources</tt> construct:
     * <code><pre>
     * try ( Locked l = lock ( myLock ) ) {
     *      // locked "myLock"
     * }
     * </pre></code>
     *
     * @param lock
     *            the lock to lock
     * @return the {@link AutoCloseable} instance which unlocks the lock
     */
    public static Locked lock ( final Lock lock )
    {
        lock.lock ();

        return new Locked () {

            @Override
            public void close ()
            {
                lock.unlock ();
            }
        };
    }

    public static <T> T call ( final Lock lock, final Supplier<T> call )
    {
        lock.lock ();
        try
        {
            return call.get ();
        }
        finally
        {
            lock.unlock ();
        }
    }
}
