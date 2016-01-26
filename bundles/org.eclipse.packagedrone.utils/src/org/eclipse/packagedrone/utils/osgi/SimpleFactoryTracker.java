/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.osgi;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SimpleFactoryTracker<S, T> extends FactoryTracker<S, T>
{
    private final Function<ServiceReference<S>, String> idFunc;

    private final BiFunction<ServiceReference<S>, S, T> serviceFunc;

    public SimpleFactoryTracker ( final BundleContext context, final Class<S> serviceClass, final Function<ServiceReference<S>, String> idFunc, final BiFunction<ServiceReference<S>, S, T> serviceFunc )
    {
        super ( context, serviceClass );
        this.idFunc = idFunc;
        this.serviceFunc = serviceFunc;
    }

    public SimpleFactoryTracker ( final BundleContext context, final Class<S> serviceClass, final Function<ServiceReference<S>, String> idFunc, final Function<S, T> serviceFunc )
    {
        this ( context, serviceClass, idFunc, ( ref, service ) -> serviceFunc.apply ( service ) );
    }

    public SimpleFactoryTracker ( final BundleContext context, final Class<S> serviceClass, final String idKey, final Function<S, T> serviceFunc )
    {
        this ( context, serviceClass, ref -> getString ( ref, idKey ), ( ref, service ) -> serviceFunc.apply ( service ) );
    }

    @Override
    protected String getFactoryId ( final ServiceReference<S> ref )
    {
        return this.idFunc.apply ( ref );
    }

    @Override
    protected T mapService ( final ServiceReference<S> reference, final S service )
    {
        return this.serviceFunc.apply ( reference, service );
    }

}
