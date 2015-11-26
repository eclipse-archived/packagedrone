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
package org.eclipse.packagedrone.repo.manage.system.web;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;

public class BundleInformation
{
    private final Bundle bundle;

    private final BundleRevision bundleRevision;

    public BundleInformation ( final Bundle bundle )
    {
        this.bundle = bundle;
        this.bundleRevision = bundle.adapt ( BundleRevision.class );
    }

    public int getState ()
    {
        return this.bundle.getState ();
    }

    public long getBundleId ()
    {
        return this.bundle.getBundleId ();
    }

    public String getSymbolicName ()
    {
        return this.bundle.getSymbolicName ();
    }

    public Version getVersion ()
    {
        return this.bundle.getVersion ();
    }

    public BundleRevision getBundleRevision ()
    {
        return this.bundleRevision;
    }

    public String getName ()
    {
        return this.bundle.getHeaders ( null ).get ( Constants.BUNDLE_NAME );
    }

    public boolean isFragment ()
    {
        return ( this.bundleRevision.getTypes () & BundleRevision.TYPE_FRAGMENT ) > 0;
    }

}
