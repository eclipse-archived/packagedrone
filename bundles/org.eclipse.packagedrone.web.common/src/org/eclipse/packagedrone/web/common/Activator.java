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
package org.eclipse.packagedrone.web.common;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.web.extender.WebExtenderProcessor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{
    private static Activator INSTANCE = null;

    private WebExtenderProcessor webExtenders;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        this.webExtenders = new WebExtenderProcessor ( context );
        INSTANCE = this;
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        if ( this.webExtenders != null )
        {
            this.webExtenders.dispose ();
            this.webExtenders = null;
        }
        INSTANCE = null;
    }

    public static void extendHead ( final HttpServletRequest request, final Writer writer ) throws IOException
    {
        INSTANCE.webExtenders.process ( ext -> ext.processHead ( request, writer ) );
    }
}
