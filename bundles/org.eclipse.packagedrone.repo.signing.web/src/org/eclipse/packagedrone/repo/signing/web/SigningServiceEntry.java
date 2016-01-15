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
package org.eclipse.packagedrone.repo.signing.web;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.packagedrone.repo.signing.SigningService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class SigningServiceEntry implements Comparable<SigningServiceEntry>
{
    private final String id;

    private final String label;

    public SigningServiceEntry ( final String id, final String label )
    {
        this.id = id;
        this.label = label == null ? id : label;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getLabel ()
    {
        return this.label;
    }

    @Override
    public int compareTo ( final SigningServiceEntry o )
    {
        return this.label.compareTo ( o.label );
    }

    @Override
    public String toString ()
    {
        if ( this.label != null )
        {
            return String.format ( "%s (%s)", this.label, this.id );
        }
        else
        {
            return this.id;
        }
    }

    public static List<SigningServiceEntry> getSigningServices ()
    {
        final List<SigningServiceEntry> result = new LinkedList<> ();

        final BundleContext ctx = FrameworkUtil.getBundle ( SigningServiceEntry.class ).getBundleContext ();
        final Collection<ServiceReference<SigningService>> refs;
        try
        {
            refs = ctx.getServiceReferences ( SigningService.class, null );
        }
        catch ( final InvalidSyntaxException e )
        {
            return Collections.emptyList ();
        }

        if ( refs != null )
        {
            for ( final ServiceReference<SigningService> ref : refs )
            {
                final String pid = makeString ( ref.getProperty ( Constants.SERVICE_PID ) );
                final String description = makeString ( ref.getProperty ( Constants.SERVICE_DESCRIPTION ) );
                result.add ( new SigningServiceEntry ( pid, description ) );
            }
        }

        return result;
    }

    public static String makeString ( final Object property )
    {
        if ( property instanceof String )
        {
            return (String)property;
        }
        return null;
    }
}
