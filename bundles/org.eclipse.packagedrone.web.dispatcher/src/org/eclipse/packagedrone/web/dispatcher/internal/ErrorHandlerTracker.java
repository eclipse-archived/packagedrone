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
package org.eclipse.packagedrone.web.dispatcher.internal;

import static java.util.Optional.ofNullable;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.ErrorHandler;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class ErrorHandlerTracker implements ErrorHandler
{
    private final ServiceTracker<ErrorHandler, ErrorHandler> tracker;

    public ErrorHandlerTracker ()
    {
        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( ErrorHandlerTracker.class ).getBundleContext (), ErrorHandler.class, null );
        this.tracker.open ();
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    @Override
    public void handleError ( final HttpServletRequest request, final HttpServletResponse response, final Throwable e ) throws ServletException, IOException
    {
        ofNullable ( this.tracker.getService () ).orElse ( DEFAULT ).handleError ( request, response, e );
    }

}
