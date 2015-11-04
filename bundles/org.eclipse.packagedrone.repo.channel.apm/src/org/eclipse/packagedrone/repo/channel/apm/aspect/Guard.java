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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import java.util.concurrent.Callable;

import org.eclipse.packagedrone.repo.utils.ThrowingRunnable;
import org.eclipse.packagedrone.utils.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Guard
{
    private final static Logger logger = LoggerFactory.getLogger ( Guard.class );

    private final ThreadLocal<Integer> state = ThreadLocal.withInitial ( () -> 0 );

    private final Runnable guardRunner;

    public Guard ( final Runnable guardRunner )
    {
        this.guardRunner = guardRunner;
    }

    public void guarded ( final ThrowingRunnable action )
    {
        guarded ( () -> {
            action.run ();
            return null;
        } );
    }

    public <T> T guarded ( final Callable<T> action )
    {
        final boolean first = push ();

        logger.trace ( "run guarded - first: {}", first );

        try
        {
            final T result = Exceptions.wrapException ( action );

            if ( first )
            {
                logger.debug ( "execute guard runner" );
                // only call if the action was successful and it was the first level
                this.guardRunner.run ();
            }

            return result;
        }
        finally
        {
            pop ();
        }
    }

    private boolean push ()
    {
        final int level = this.state.get ();
        this.state.set ( level + 1 );
        return level == 0;
    }

    private void pop ()
    {
        this.state.set ( this.state.get () - 1 );
    }
}
