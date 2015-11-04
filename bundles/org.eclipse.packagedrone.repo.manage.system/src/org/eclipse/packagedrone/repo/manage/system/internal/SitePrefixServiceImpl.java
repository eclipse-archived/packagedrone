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
package org.eclipse.packagedrone.repo.manage.system.internal;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.manage.core.CoreService;
import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.repo.manage.system.SystemService;

public class SitePrefixServiceImpl implements SitePrefixService
{
    private static final MetaKey KEY_SITE_PREFIX = new MetaKey ( "core", "site-prefix" );

    private CoreService coreService;

    private SystemService systemService;

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    public void unsetCoreService ( final CoreService coreService )
    {
        this.coreService = null;
    }

    public void setSystemService ( final SystemService systemService )
    {
        this.systemService = systemService;
    }

    public void unsetSystemService ( final SystemService systemService )
    {
        this.systemService = null;
    }

    @Override
    public String getSitePrefix ()
    {
        final CoreService coreService = this.coreService;

        if ( coreService != null )
        {
            final String prefix = coreService.getCoreProperty ( KEY_SITE_PREFIX );
            if ( prefix != null )
            {
                return prefix;
            }
        }

        final SystemService systemService = this.systemService;
        if ( systemService != null )
        {
            final String prefix = systemService.getDefaultSitePrefix ();
            if ( prefix != null )
            {
                return prefix;
            }
        }

        return System.getProperty ( "drone.fallbackSitePrefix", "http://localhost:8080" );
    }
}
