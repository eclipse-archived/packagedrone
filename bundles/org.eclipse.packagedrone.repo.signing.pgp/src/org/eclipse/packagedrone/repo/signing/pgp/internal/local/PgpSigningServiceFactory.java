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
package org.eclipse.packagedrone.repo.signing.pgp.internal.local;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

public class PgpSigningServiceFactory implements ManagedServiceFactory
{
    private final Map<String, Entry> services = new HashMap<> ();

    private final BundleContext context = FrameworkUtil.getBundle ( PgpSigningServiceFactory.class ).getBundleContext ();

    @Override
    public String getName ()
    {
        return "PGP Signing Service Factory";
    }

    @Override
    public void updated ( final String pid, final Dictionary<String, ?> properties ) throws ConfigurationException
    {
        Entry entry = this.services.get ( pid );
        if ( entry == null )
        {
            entry = new Entry ( pid, this.context );
            this.services.put ( pid, entry );
        }
        entry.update ( properties );
    }

    @Override
    public void deleted ( final String pid )
    {
        final Entry service = this.services.remove ( pid );
        if ( service != null )
        {
            service.dispose ();
        }
    }

}
