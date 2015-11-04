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
package org.eclipse.packagedrone.utils;

import java.lang.management.ManagementFactory;

public final class ProcessUtil
{
    private static Long pid;

    private static boolean initialized;

    private ProcessUtil ()
    {
    }

    public static Long getProcessId ()
    {
        if ( !initialized )
        {
            pid = getProcessIdFromMBean ();
            initialized = true;
        }
        return pid;
    }

    private static Long getProcessIdFromMBean ()
    {
        try
        {
            return Long.parseLong ( ManagementFactory.getRuntimeMXBean ().getName ().split ( "@" )[0] );
        }
        catch ( final Throwable e )
        {
            return null;
        }
    }
}
