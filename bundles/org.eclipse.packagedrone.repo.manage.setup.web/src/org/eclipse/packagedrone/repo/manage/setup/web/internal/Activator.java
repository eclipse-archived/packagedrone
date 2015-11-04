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
package org.eclipse.packagedrone.repo.manage.setup.web.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator
{

    private ServiceTracker<?, ?> tracker;

    private static Activator INSTANCE;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        this.tracker = new ServiceTracker<Object, Object> ( context, "org.eclipse.packagedrone.repo.channel.ChannelService", null );
        this.tracker.open ( true );
        INSTANCE = this;
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        INSTANCE = null;
        this.tracker.close ();
        this.tracker = null;
    }

    public static ServiceTracker<?, ?> getTracker ()
    {
        return INSTANCE.tracker;
    }

}
