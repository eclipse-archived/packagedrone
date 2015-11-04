/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.manage.setup.web;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.manage.setup.web.internal.Activator;
import org.eclipse.packagedrone.web.interceptor.ModelAndViewInterceptorAdapter;
import org.eclipse.packagedrone.web.util.Requests;
import org.osgi.util.tracker.ServiceTracker;

public class SetupInterceptor extends ModelAndViewInterceptorAdapter
{
    private final ServiceTracker<?, ?> tracker;

    private final Set<String> ignoredPrefixes = new HashSet<> ();

    public SetupInterceptor ()
    {
        this.ignoredPrefixes.add ( "/setup" );
        this.ignoredPrefixes.add ( "/login" );
        this.ignoredPrefixes.add ( "/logout" );
        this.ignoredPrefixes.add ( "/resources" );
        this.ignoredPrefixes.add ( "/system/backup" );

        this.tracker = Activator.getTracker ();
    }

    @Override
    public boolean preHandle ( final HttpServletRequest request, final HttpServletResponse response ) throws Exception
    {
        final String current = Requests.getOriginalPath ( request );

        if ( current == null )
        {
            response.sendRedirect ( request.getContextPath () + "/setup" );
            return false;
        }

        for ( final String prefix : this.ignoredPrefixes )
        {
            if ( current.startsWith ( prefix ) )
            {
                return super.preHandle ( request, response );
            }
        }

        if ( current.startsWith ( "/config" ) && request.getUserPrincipal () != null )
        {
            // this is a special case where the user is logged in, but the system might not be fully functional. In this case let him pass.
            return super.preHandle ( request, response );
        }

        if ( this.tracker.getService () == null )
        {
            response.sendRedirect ( request.getContextPath () + "/setup" );
            return false;
        }
        else
        {
            return super.preHandle ( request, response );
        }
    }
}
