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
package org.eclipse.packagedrone;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

public final class VersionInformation
{
    public static final String VERSION;

    public static final String VERSION_UNQUALIFIED;

    public static final String USER_AGENT;

    private VersionInformation ()
    {
    }

    static
    {
        final Version version = FrameworkUtil.getBundle ( VersionInformation.class ).getVersion ();
        VERSION = version.toString ();
        VERSION_UNQUALIFIED = new Version ( version.getMajor (), version.getMinor (), version.getMicro () ).toString ();

        USER_AGENT = String.format ( "PackageDrone/%s (+http://packagedrone.org)", VersionInformation.VERSION_UNQUALIFIED );
    }
}
