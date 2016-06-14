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
package org.eclipse.packagedrone.repo.xml.internal;

import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{
    private static XmlToolsFactory factory;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        Activator.factory = new XmlToolsFactoryImpl ();
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        Activator.factory = null;
    }

    public static XmlToolsFactory getXmlToolsFactory ()
    {
        final XmlToolsFactory tools = factory;

        if ( tools == null )
        {
            throw new IllegalStateException ( String.format ( "There is no instance of '%s' registered.", XmlToolsFactory.class.getSimpleName () ) );
        }

        return tools;
    }

}
