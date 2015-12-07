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
package org.eclipse.packagedrone.repo.channel.servlet;

import static org.eclipse.packagedrone.web.util.BasicAuthentication.parseAuthorization;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.web.util.BasicAuthentication;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract implementation for implementing servlets which require
 * the {@link StorageService}.
 * <p>
 * The servlet ensures that the service methods (GET, POST, ... ) only get
 * called when there is a storage service present. The service can then be
 * fetched using {@link #getService(HttpServletRequest)}.
 * </p<
 */
public abstract class AbstractChannelServiceServlet extends HttpServlet
{
    private final static Logger logger = LoggerFactory.getLogger ( AbstractChannelServiceServlet.class );

    private static final long serialVersionUID = 1L;

    private static final String ATTR_CHANNEL_SERVICE = AbstractChannelServiceServlet.class.getName () + ".channelService";

    private ServiceTracker<ChannelService, ChannelService> tracker;

    public AbstractChannelServiceServlet ()
    {
        super ();
    }

    @Override
    public void init () throws ServletException
    {
        super.init ();

        final BundleContext context = FrameworkUtil.getBundle ( getClass () ).getBundleContext ();
        this.tracker = new ServiceTracker<> ( context, ChannelService.class, null );
        this.tracker.open ();
    }

    protected ChannelService getService ( final HttpServletRequest request )
    {
        return (ChannelService)request.getAttribute ( ATTR_CHANNEL_SERVICE );
    }

    @Override
    protected void service ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        logger.trace ( "Request: {} / {}", request.getMethod (), request.getPathInfo () );

        final ChannelService service = this.tracker.getService ();

        if ( service == null )
        {
            handleNoService ( request, response );
        }
        else
        {
            request.setAttribute ( ATTR_CHANNEL_SERVICE, service );
            super.service ( request, response );
        }
    }

    protected void handleNoService ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
        response.setContentType ( "text/plain" );
        response.getWriter ().write ( "Channel service unavailable" );
    }

    @Override
    public void destroy ()
    {
        this.tracker.close ();
        super.destroy ();
    }

    /**
     * Authenticate the request is authenticated against the deploy keys
     * <p>
     * If the request could not be authenticated a basic authentication request
     * is sent back and the {@link HttpServletResponse} will be committed.
     * </p>
     *
     * @param by
     *            the channel locator
     * @param request
     *            the request
     * @param response
     *            the response
     * @return <code>false</code> if the request was not authenticated and the
     *         response got committed, <code>true</code> otherwise
     * @throws IOException
     *             in case on a IO error
     */
    protected boolean authenticate ( final By by, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        if ( isAuthenticated ( by, request ) )
        {
            return true;
        }

        BasicAuthentication.request ( response, "channel", "Please authenticate" );

        return false;
    }

    /**
     * Simply test if the request is authenticated against the channels deploy
     * keys
     *
     * @param by
     *            the channel locator
     * @param request
     *            the request
     * @return <code>true</code> if the request could be authenticated against
     *         the channels deploy keys, <code>false</code> otherwise
     */
    protected boolean isAuthenticated ( final By by, final HttpServletRequest request )
    {
        final String[] authToks = parseAuthorization ( request );

        if ( authToks == null )
        {
            return false;
        }

        if ( !authToks[0].equals ( "deploy" ) )
        {
            return false;
        }

        final String deployKey = authToks[1];

        logger.debug ( "Deploy key: '{}'", deployKey );

        final ChannelService service = getService ( request );
        if ( service == null )
        {
            logger.info ( "Called 'isAuthenticated' without service" );
            return false;
        }

        return service.getChannelDeployKeyStrings ( by ).orElse ( Collections.emptySet () ).contains ( deployKey );
    }

}
