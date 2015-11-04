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
package org.eclipse.packagedrone.repo.internal;

import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator
{
    private ServiceTracker<XmlToolsFactory, XmlToolsFactory> tracker;

    private static Activator instance;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        this.tracker = new ServiceTracker<> ( context, XmlToolsFactory.class, null );
        this.tracker.open ();
        instance = this;
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        instance = null;
        this.tracker.close ();
        this.tracker = null;
    }

    public static XmlToolsFactory getXmlToolsFactory ()
    {
        final XmlToolsFactory tools = instance.tracker.getService ();

        if ( tools == null )
        {
            throw new IllegalStateException ( String.format ( "There is no instance of '%s' registered.", XmlToolsFactory.class.getSimpleName () ) );
        }

        return tools;
    }

}
