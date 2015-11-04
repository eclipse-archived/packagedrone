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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class SchedulerThreadFactory implements ThreadFactory
{
    private static AtomicLong COUNTER = new AtomicLong ();

    @Override
    public Thread newThread ( final Runnable r )
    {
        final Thread t = new Thread ( r );

        t.setName ( "scheduler/" + COUNTER.incrementAndGet () );

        return t;
    }

}
