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
package org.eclipse.packagedrone.repo.adapter.npm;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.manage.system.SystemService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class NpmServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private ServiceTracker<ChannelService, ChannelService> tracker;

    private ServiceTracker<SystemService, SystemService> sysTracker;

    @Override
    public void init () throws ServletException
    {
        super.init ();

        final BundleContext context = FrameworkUtil.getBundle ( NpmServlet.class ).getBundleContext ();

        this.tracker = new ServiceTracker<> ( context, ChannelService.class, null );
        this.tracker.open ();

        this.sysTracker = new ServiceTracker<> ( context, SystemService.class, null );
        this.sysTracker.open ();
    }

    @Override
    public void destroy ()
    {
        this.tracker.close ();
        this.sysTracker.close ();

        super.destroy ();
    }

    @Override
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        String path = request.getPathInfo ();

        if ( path == null )
        {
            path = "/";
        }

        if ( "/".equals ( path ) )
        {
            response.getWriter ().write ( "NPM adapter" );
            return;
        }

        final ChannelService service = this.tracker.getService ();
        final SystemService sysService = this.sysTracker.getService ();

        if ( service == null || sysService == null )
        {
            response.getWriter ().write ( "System not operational" );
            response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
            return;
        }

        path = path.replaceAll ( "^/+", "" );

        final String toks[] = path.split ( "/" );

        if ( toks.length < 1 )
        {
            notFound ( request, response );
            return;
        }

        try
        {
            service.accessRun ( By.nameOrId ( toks[0] ), ReadableChannel.class, channel -> {
                processGet ( request, response, sysService, channel, toks );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            response.getWriter ().format ( "Channel '%s' not found", toks[0] );
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        }
    }

    private void processGet ( final HttpServletRequest request, final HttpServletResponse response, final SystemService service, final ReadableChannel channel, final String[] toks ) throws IOException
    {
        if ( toks.length == 2 )
        {
            new ModuleHandler ( service, channel, toks[1], true ).process ( response );
            return;
        }

        response.getWriter ().write ( "No handler" );
        response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
    }

    private void notFound ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        response.getWriter ().format ( "Resource '%s' not found", request.getPathInfo () );
        response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
    }

}
