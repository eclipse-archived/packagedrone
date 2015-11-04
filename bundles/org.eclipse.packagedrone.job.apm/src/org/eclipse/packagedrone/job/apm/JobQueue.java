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
package org.eclipse.packagedrone.job.apm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobQueue
{
    private final static Logger logger = LoggerFactory.getLogger ( JobQueue.class );

    private static AtomicLong GLOBAL_COUNTER = new AtomicLong ();

    public class ThreadFactoryImpl implements ThreadFactory
    {
        private final long globalCounter;

        public ThreadFactoryImpl ( final long globalCounter )
        {
            this.globalCounter = globalCounter;
        }

        @Override
        public Thread newThread ( final Runnable r )
        {
            final Thread t = new Thread ( r, makeName () );
            return t;
        }

        private String makeName ()
        {
            return String.format ( "job-processor/%s/%s", this.globalCounter, localCounter.incrementAndGet () );
        }
    }

    private static AtomicLong localCounter = new AtomicLong ();

    private ExecutorService executor;

    public void start ()
    {
        this.executor = Executors.newSingleThreadExecutor ( new ThreadFactoryImpl ( GLOBAL_COUNTER.incrementAndGet () ) );
    }

    public void stop ()
    {
        this.executor.shutdown ();
        try
        {
            this.executor.awaitTermination ( Long.MAX_VALUE, TimeUnit.MILLISECONDS );
        }
        catch ( final InterruptedException e )
        {
            // stopped waiting ...
        }
    }

    public void push ( final Runnable task )
    {
        logger.debug ( "Pushing task: {}", task );

        this.executor.execute ( task );
    }

}
