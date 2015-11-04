/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class OsgiValidationProviderTracker implements ValidationProviderResolver
{
    @SuppressWarnings ( "rawtypes" )
    private final ServiceTracker<ValidationProvider, ValidationProvider<?>> tracker;

    public OsgiValidationProviderTracker ()
    {
        final BundleContext context = FrameworkUtil.getBundle ( OsgiValidationProviderTracker.class ).getBundleContext ();

        this.tracker = new ServiceTracker<> ( context, ValidationProvider.class, null );
    }

    public void open ()
    {
        this.tracker.open ();
    }

    public void close ()
    {
        this.tracker.close ();
    }

    public int getTrackingCount ()
    {
        return this.tracker.getTrackingCount ();
    }

    @Override
    public List<ValidationProvider<?>> getValidationProviders ()
    {
        return new ArrayList<> ( this.tracker.getTracked ().values () );
    }
}
