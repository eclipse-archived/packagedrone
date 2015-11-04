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
package org.eclipse.packagedrone.web.dispatcher;

import org.eclipse.packagedrone.web.dispatcher.internal.ServletContextImpl;
import org.osgi.framework.BundleContext;

public class Dispatcher
{
    public static DispatcherHttpContext createContext ( final BundleContext context )
    {
        return new ServletContextImpl ( context );
    }
}
