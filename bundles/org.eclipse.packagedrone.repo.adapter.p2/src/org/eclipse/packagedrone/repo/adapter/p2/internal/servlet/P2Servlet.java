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
package org.eclipse.packagedrone.repo.adapter.p2.internal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.p2.internal.aspect.ChannelStreamer;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.servlet.AbstractChannelServiceServlet;
import org.eclipse.packagedrone.repo.channel.web.utils.ChannelCacheHandler;
import org.eclipse.packagedrone.repo.servlet.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2Servlet extends AbstractChannelServiceServlet
{
    private static final Logger logger = LoggerFactory.getLogger ( P2Servlet.class );

    private static final long serialVersionUID = 1L;

    private final ChannelCacheHandler artifactsXml = new ChannelCacheHandler ( new MetaKey ( "p2.repo", "artifacts.xml" ) );

    private final ChannelCacheHandler artifactsJar = new ChannelCacheHandler ( new MetaKey ( "p2.repo", "artifacts.jar" ) );

    private final ChannelCacheHandler contentXml = new ChannelCacheHandler ( new MetaKey ( "p2.repo", "content.xml" ) );

    private final ChannelCacheHandler contentJar = new ChannelCacheHandler ( new MetaKey ( "p2.repo", "content.jar" ) );

    @Override
    protected void service ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        logger.debug ( "Request: {} / {}", req.getMethod (), req.getServletPath () );
        super.service ( req, resp );
    }

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        final String path;
        if ( req.getPathInfo () == null )
        {
            path = "/";
        }
        else
        {
            path = req.getPathInfo ();
        }

        final String paths[] = path.split ( "/" );

        if ( paths.length < 2 )
        {
            showHelp ( resp );
            return;
        }

        final String channelIdOrName = decode ( paths[1] );
        final ChannelService service = getService ( req );

        try
        {
            service.accessRun ( By.nameOrId ( channelIdOrName ), ReadableChannel.class, channel -> {

                final String channelId = channel.getId ().getId ();

                if ( paths.length < 3 )
                {
                    if ( !path.endsWith ( "/" ) )
                    {
                        resp.setStatus ( HttpServletResponse.SC_MOVED_PERMANENTLY );
                        resp.sendRedirect ( req.getRequestURI () + "/" );
                        return;
                    }
                    final String title = ChannelStreamer.makeTitle ( channel.getId ().getId (), channel.getMetaData () );
                    req.setAttribute ( "p2Title", title );
                    req.setAttribute ( "id", channel.getId ().getId () );
                    req.setAttribute ( "name", channel.getId ().getName () );
                    req.setAttribute ( "description", channel.getInformation ().getState ().getDescription () );
                    req.getRequestDispatcher ( "/WEB-INF/views/channel.jsp" ).forward ( req, resp );
                }
                else if ( "p2.index".equals ( paths[2] ) && paths.length == 3 )
                {
                    req.getRequestDispatcher ( "/WEB-INF/views/p2index.jsp" ).forward ( req, resp );
                }
                else if ( "content.xml".equals ( paths[2] ) && paths.length == 3 )
                {
                    this.contentXml.process ( channel, req, resp );
                }
                else if ( "artifacts.xml".equals ( paths[2] ) && paths.length == 3 )
                {
                    this.artifactsXml.process ( channel, req, resp );
                }
                else if ( "content.jar".equals ( paths[2] ) && paths.length == 3 )
                {
                    this.contentJar.process ( channel, req, resp );
                }
                else if ( "artifacts.jar".equals ( paths[2] ) && paths.length == 3 )
                {
                    this.artifactsJar.process ( channel, req, resp );
                }
                else if ( "repo.zip".equals ( paths[2] ) && paths.length == 3 )
                {
                    process ( req, resp, new ZippedHandler ( channel ) );
                }
                else if ( paths.length == 6 && "plugins".equals ( paths[2] ) )
                {
                    logger.debug ( "Download plugin: {}", req.getPathInfo () );
                    final String id = paths[3];
                    final String version = paths[4];
                    final String fileName = paths[5];
                    process ( req, resp, new DownloadHandler ( channelId, service, id, version, fileName, "bundle" ) );
                }
                else if ( paths.length == 6 && "features".equals ( paths[2] ) )
                {
                    logger.debug ( "Download feature: {}", path );
                    final String id = paths[3];
                    final String version = paths[4];
                    final String fileName = paths[5];
                    process ( req, resp, new DownloadHandler ( channelId, service, id, version, fileName, "eclipse.feature" ) );
                }
                else
                {
                    logger.info ( "Not found for: {}", path );
                    notFound ( req, resp, "Resource not found: " + path );
                }
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            notFound ( req, resp, String.format ( "Channel '%s' not found.", channelIdOrName ) );
            return;
        }

    }

    private String decode ( final String string )
    {
        try
        {
            return URLDecoder.decode ( string, "UTF-8" );
        }
        catch ( final UnsupportedEncodingException e )
        {
            throw new IllegalStateException ( e );
        }
    }

    protected void notFound ( final HttpServletRequest req, final HttpServletResponse resp, final String message ) throws IOException
    {
        resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );

        @SuppressWarnings ( "resource" )
        final PrintWriter w = resp.getWriter ();
        resp.setContentType ( "text/plain" );

        w.println ( message );
    }

    private void showHelp ( final HttpServletResponse resp ) throws IOException
    {
        resp.setStatus ( HttpServletResponse.SC_OK );
        resp.getWriter ().println ( "This is the package drone P2 adapter.\n\nAlways know where your towel is!" );
    }

    private void process ( final HttpServletRequest req, final HttpServletResponse resp, final Handler handler ) throws ServletException
    {
        try
        {
            handler.process ( req, resp );
        }
        catch ( final Exception e )
        {
            throw new ServletException ( e );
        }
    }

}
