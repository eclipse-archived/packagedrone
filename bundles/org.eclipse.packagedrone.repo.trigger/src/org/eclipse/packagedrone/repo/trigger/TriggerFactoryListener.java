/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.trigger;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class TriggerFactoryListener
{
    private final BundleContext context;

    private final ServiceListener listener = new ServiceListener () {

        @Override
        public void serviceChanged ( final ServiceEvent event )
        {
            switch ( event.getType () )
            {
                case ServiceEvent.REGISTERED:
                    addService ( event.getServiceReference () );
                    break;
                case ServiceEvent.MODIFIED_ENDMATCH:
                case ServiceEvent.UNREGISTERING:
                    removeService ( event.getServiceReference () );
                    break;
            }
        }
    };

    private final Map<ServiceReference<?>, TriggerFactory> services = new HashMap<> ();

    public TriggerFactoryListener ( final BundleContext context )
    {
        this.context = context;
    }

    protected void addService ( final ServiceReference<?> ref )
    {
        if ( this.services.containsKey ( ref ) )
        {
            return;
        }

        final Object so = this.context.getService ( ref );
        if ( ! ( so instanceof TriggerFactory ) )
        {
            this.context.ungetService ( ref );
            return;
        }

        final TriggerFactory service = (TriggerFactory)so;
        this.services.put ( ref, service );

        serviceAdded ( service );
    }

    protected void removeService ( final ServiceReference<?> ref )
    {
        final TriggerFactory service = this.services.remove ( ref );
        if ( service == null )
        {
            return;
        }

        this.context.ungetService ( ref );

        serviceRemoved ( service );
    }

    protected void serviceAdded ( final TriggerFactory service )
    {
    }

    protected void serviceRemoved ( final TriggerFactory service )
    {

    }

    public void start ()
    {
        try
        {
            this.context.addServiceListener ( this.listener, String.format ( "(%s=%s)", Constants.OBJECTCLASS, TriggerFactory.class.getName () ) );
        }
        catch ( final InvalidSyntaxException e )
        {
            // should never happen *sigh*
            throw new RuntimeException ( e );
        }
        // FIXME: send out notifies
    }

    public void stop ()
    {
        this.context.removeServiceListener ( this.listener );
    }
}
