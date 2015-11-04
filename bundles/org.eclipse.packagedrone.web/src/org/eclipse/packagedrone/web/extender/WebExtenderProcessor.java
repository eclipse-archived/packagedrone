/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.extender;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class WebExtenderProcessor
{
    private final BundleContext context;

    private final ServiceTracker<WebExtender, WebExtender> tracker;

    @FunctionalInterface
    public interface Consumer
    {
        public void process ( WebExtender extender ) throws IOException;
    }

    private final ServiceTrackerCustomizer<WebExtender, WebExtender> customizer = new ServiceTrackerCustomizer<WebExtender, WebExtender> () {

        @Override
        public void removedService ( final ServiceReference<WebExtender> reference, final WebExtender service )
        {
            removeService ( service );
            WebExtenderProcessor.this.context.ungetService ( reference );
        }

        @Override
        public void modifiedService ( final ServiceReference<WebExtender> reference, final WebExtender service )
        {
        }

        @Override
        public WebExtender addingService ( final ServiceReference<WebExtender> reference )
        {
            final WebExtender service = WebExtenderProcessor.this.context.getService ( reference );
            addService ( service );
            return service;
        }
    };

    private final Set<WebExtender> extenders = new CopyOnWriteArraySet<> ();

    public WebExtenderProcessor ( final BundleContext context )
    {
        this.context = context;

        this.tracker = new ServiceTracker<> ( context, WebExtender.class, this.customizer );
        this.tracker.open ();
    }

    protected void addService ( final WebExtender service )
    {
        this.extenders.add ( service );
    }

    protected void removeService ( final WebExtender service )
    {
        this.extenders.remove ( service );
    }

    public void process ( final Consumer consumer ) throws IOException
    {
        for ( final WebExtender extender : this.extenders )
        {
            consumer.process ( extender );
        }
    }

    public void dispose ()
    {
        this.tracker.close ();
    }
}
