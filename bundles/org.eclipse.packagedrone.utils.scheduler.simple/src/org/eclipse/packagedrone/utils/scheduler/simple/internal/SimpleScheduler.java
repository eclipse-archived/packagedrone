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
package org.eclipse.packagedrone.utils.scheduler.simple.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.packagedrone.utils.scheduler.Constants;
import org.eclipse.packagedrone.utils.scheduler.ScheduledTask;
import org.eclipse.packagedrone.utils.scheduler.simple.Scheduler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleScheduler implements Scheduler
{
    private final static Logger logger = LoggerFactory.getLogger ( SimpleScheduler.class );

    private ScheduledExecutorService executor;

    protected static Long getPeriod ( final ServiceReference<?> ref )
    {
        final Object periodMsVal = ref.getProperty ( Constants.PERIOD_MS );
        if ( periodMsVal instanceof Number )
        {
            return ( (Number)periodMsVal ).longValue ();
        }
        else if ( periodMsVal instanceof String )
        {
            try
            {
                return Long.parseLong ( (String)periodMsVal );
            }
            catch ( final NumberFormatException e )
            {
            }
        }

        return null;
    }

    private final ServiceTrackerCustomizer<ScheduledTask, Entry> scheduledCustomizer = new ServiceTrackerCustomizer<ScheduledTask, SimpleScheduler.Entry> () {

        @Override
        public void removedService ( final ServiceReference<ScheduledTask> reference, final Entry service )
        {
            service.dispose ();
        }

        @Override
        public void modifiedService ( final ServiceReference<ScheduledTask> reference, final Entry service )
        {
            service.reschedule ( getPeriod ( reference ) );
        }

        @Override
        public Entry addingService ( final ServiceReference<ScheduledTask> reference )
        {
            final BundleContext context = FrameworkUtil.getBundle ( SimpleScheduler.class ).getBundleContext ();

            final Entry entry = new Entry ( context.getService ( reference ) );
            entry.reschedule ( getPeriod ( reference ) );
            return entry;
        }
    };

    private final ServiceTracker<ScheduledTask, Entry> tracker;

    private class Entry implements Runnable
    {
        private final ScheduledTask service;

        private ScheduledFuture<?> job;

        public Entry ( final ScheduledTask service )
        {
            this.service = service;
            logger.info ( "Created job: {}", service );
        }

        public void reschedule ( final Long period )
        {
            logger.info ( "Reschedule job: {} ms - {}", period, this.service );

            if ( period != null && period > 0 )
            {
                if ( this.job != null )
                {
                    logger.debug ( "Cancel old job" );
                    this.job.cancel ( false );
                }
                this.job = SimpleScheduler.this.executor.scheduleAtFixedRate ( this, 0, period, TimeUnit.MILLISECONDS );
                logger.debug ( "Started new job" );
            }
            else
            {
                if ( this.job != null )
                {
                    logger.debug ( "Cancel job" );
                    this.job.cancel ( false );
                    this.job = null;
                }
            }
        }

        public void dispose ()
        {
            reschedule ( null );
        }

        @Override
        public void run ()
        {
            logger.debug ( "Running job {} ... ", this.service );
            try
            {
                this.service.run ();
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
            logger.debug ( "Running job {} ... done!", this.service );
        }

    }

    public SimpleScheduler ()
    {
        final BundleContext context = FrameworkUtil.getBundle ( SimpleScheduler.class ).getBundleContext ();

        this.tracker = new ServiceTracker<> ( context, ScheduledTask.class, this.scheduledCustomizer );
    }

    public void start ()
    {
        logger.warn ( "Starting ... " );

        this.executor = Executors.newSingleThreadScheduledExecutor ( new SchedulerThreadFactory () );
        this.tracker.open ();
    }

    public void stop ()
    {
        logger.warn ( "Stopping ... " );

        this.tracker.close ();
        if ( this.executor != null )
        {
            this.executor.shutdown ();
            this.executor = null;
        }
    }
}
