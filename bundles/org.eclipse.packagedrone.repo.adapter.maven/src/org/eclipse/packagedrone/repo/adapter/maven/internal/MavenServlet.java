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
package org.eclipse.packagedrone.repo.adapter.maven.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData;
import org.eclipse.packagedrone.repo.adapter.maven.upload.ArtifactNotFoundException;
import org.eclipse.packagedrone.repo.adapter.maven.upload.ChannelUploadTarget;
import org.eclipse.packagedrone.repo.adapter.maven.upload.ChecksumValidationException;
import org.eclipse.packagedrone.repo.adapter.maven.upload.Uploader;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.servlet.AbstractChannelServiceServlet;
import org.eclipse.packagedrone.repo.web.utils.ChannelCacheHandler;
import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;
import org.eclipse.scada.utils.lang.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenServlet extends AbstractChannelServiceServlet
{
    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger ( MavenServlet.class );

    private static final MetaKey CHANNEL_KEY = new MetaKey ( "maven.repo", "channel" );

    private static final ChannelCacheHandler HANDLER_REPO_META = new ChannelCacheHandler ( new MetaKey ( "maven.repo", "repo-metadata" ) );

    private static final ChannelCacheHandler HANDLER_PREFIXES = new ChannelCacheHandler ( new MetaKey ( "maven.repo", "prefixes" ) );

    private static String makeOperation ( final HttpServletRequest request )
    {
        return String.format ( "MavenServlet|%s|%s", request.getRequestURI (), request.getMethod () );
    }

    @Override
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        logger.trace ( "get request - {}", request );

        try ( Handle handle = Profile.start ( makeOperation ( request ) ) )
        {
            handleGetRequest ( request, response );
        }
    }

    private void handleGetRequest ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        String pathString = request.getPathInfo ();
        if ( pathString == null )
        {
            pathString = "/";
        }

        if ( "/".equals ( pathString ) )
        {
            reply ( response, HttpServletResponse.SC_OK, "Package Drone Maven 2 Repository Adapter" );
            return;
        }

        final ChannelService service = getService ( request ); // ensured not be null

        pathString = pathString.replaceAll ( "^/+", "" );
        pathString = pathString.replaceAll ( "/+$", "" );

        final String[] toks = pathString.split ( "/+", 2 );
        final String channelId = toks[0];

        try
        {
            service.accessRun ( By.nameOrId ( channelId ), ReadableChannel.class, channel -> {

                // init holder

                final Holder<ChannelData> holder = new Holder<> ();

                // fetch structure from cache

                try
                {
                    if ( !channel.streamCacheEntry ( CHANNEL_KEY, entry -> {
                        holder.value = ChannelData.fromReader ( new InputStreamReader ( entry.getStream (), StandardCharsets.UTF_8 ) );
                    } ) )
                    {
                        commitNotConfigured ( response, channelId );
                        return;
                    }
                }
                catch ( final Exception e )
                {
                    logger.warn ( "Failed to load maven channel data", e );
                    reply ( response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Corrupt channel data" );
                    return;
                }

                // get data

                final ChannelData channelData = holder.value;

                // check for null

                if ( channelData == null )
                {
                    logger.debug ( "No maven channel data: {}", channel.getId () );
                    commitNotConfigured ( response, channelId );
                    return;
                }

                if ( toks.length == 2 && toks[1].equals ( ".meta/repository-metadata.xml" ) )
                {
                    HANDLER_REPO_META.process ( channel, request, response );
                    return;
                }

                if ( toks.length == 2 && toks[1].equals ( ".meta/prefixes.txt" ) )
                {
                    HANDLER_PREFIXES.process ( channel, request, response );
                    return;
                }

                new MavenHandler ( channel, channelData ).handle ( toks.length > 1 ? toks[1] : null, request, response );

            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            reply ( response, HttpServletResponse.SC_NOT_FOUND, "Channel %s not found", channelId );
            return;
        }
    }

    protected void commitNotConfigured ( final HttpServletResponse response, final String channelId ) throws IOException
    {
        reply ( response, HttpServletResponse.SC_NOT_FOUND, "Channel %s is not configured for providing a Maven 2 repository. Add the Maven Repository aspect!", channelId );
    }

    @Override
    protected void doPut ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        try ( Handle handle = Profile.start ( makeOperation ( request ) ) )
        {
            logger.debug ( "Request - pathInfo: {} ", request.getPathInfo () );

            processPut ( request, response, getService ( request ) );
        }
        catch ( final IOException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw new ServletException ( e );
        }
    }

    private void processUpload ( final ModifiableChannel channel, final String path, final HttpServletRequest request ) throws ChecksumValidationException, IOException
    {
        final Uploader uploader = new Uploader ( new ChannelUploadTarget ( channel ), null );
        uploader.receive ( path, request.getInputStream () );
    }

    private void processPut ( final HttpServletRequest request, final HttpServletResponse response, final ChannelService service ) throws Exception
    {
        final String[] toks = request.getPathInfo ().split ( "/+", 3 );

        if ( toks.length != 3 )
        {
            logger.debug ( "Upload path: {}", new Object[] { toks } );
            reply ( response, HttpServletResponse.SC_NOT_FOUND, "Upload path not found: %s", new Object[] { toks } );
            return;
        }

        final String channelNameOrId = toks[1];
        final String fullName = toks[2];

        logger.debug ( "Channel: {}, Artifact: {}", channelNameOrId, fullName );

        if ( !authenticate ( By.nameOrId ( channelNameOrId ), request, response ) )
        {
            return;
        }

        try
        {
            service.accessRun ( By.nameOrId ( channelNameOrId ), ModifiableChannel.class, channel -> {
                try
                {
                    processUpload ( channel, fullName, request );
                    response.setStatus ( HttpServletResponse.SC_OK );
                }
                catch ( final ArtifactNotFoundException e )
                {
                    logger.debug ( "Artifact not found", e );

                    // simply ignoring for now, later we could add a policy for this
                    reply ( response, HttpServletResponse.SC_OK, e.getMessage () );
                }
                catch ( final ChecksumValidationException e )
                {
                    logger.info ( "Checksum validation failed", e );

                    reply ( response, HttpServletResponse.SC_NOT_ACCEPTABLE, e.getMessage () );
                    return;
                }
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            reply ( response, HttpServletResponse.SC_NOT_FOUND, "Channel %s not found", channelNameOrId );
            return;
        }

    }

    private static void reply ( final HttpServletResponse response, final int statusCode, final String message, final Object... args ) throws IOException
    {
        response.setStatus ( statusCode );
        response.setContentType ( "text/plain" );
        response.getWriter ().format ( message, args );
    }

}
