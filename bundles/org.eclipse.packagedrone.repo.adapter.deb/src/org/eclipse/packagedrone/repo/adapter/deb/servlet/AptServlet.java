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
package org.eclipse.packagedrone.repo.adapter.deb.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.adapter.deb.ChannelConfiguration;
import org.eclipse.packagedrone.repo.adapter.deb.aspect.AptChannelAspectFactory;
import org.eclipse.packagedrone.repo.adapter.deb.servlet.handler.ChannelCacheHandler;
import org.eclipse.packagedrone.repo.adapter.deb.servlet.handler.ContentHandler;
import org.eclipse.packagedrone.repo.adapter.deb.servlet.handler.Handler;
import org.eclipse.packagedrone.repo.adapter.deb.servlet.handler.PoolHandler;
import org.eclipse.packagedrone.repo.adapter.deb.servlet.handler.RedirectHandler;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.servlet.AbstractChannelServiceServlet;

public class AptServlet extends AbstractChannelServiceServlet
{
    private static final long serialVersionUID = 1L;

    private static final Pattern POOL_PATTERN = Pattern.compile ( "pool/(?<aid>[^/]+)/(?<name>.*)" );

    @Override
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        final ChannelService service = getService ( request );

        String path = request.getPathInfo ();

        if ( path == null )
        {
            path = "/";
        }

        // strip of leading slash
        path = path.replaceAll ( "^/+", "" );
        path = path.replaceAll ( "/+$", "" );

        final String[] toks = path.split ( "/", 2 );

        if ( path.isEmpty () || toks.length == 0 )
        {
            response.setContentType ( "text/plain" );
            response.getWriter ().write ( "Package Drone - APT repository adapter" );
            return;
        }

        final String channelId = toks[0];

        try
        {
            service.accessRun ( By.nameOrId ( channelId ), ReadableChannel.class, channel -> {

                if ( !channel.hasAspect ( AptChannelAspectFactory.ID ) )
                {
                    response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
                    response.getWriter ().format ( "Channel '%s' is not configured as APT repository. Add the \"APT Repository\" channel aspect.", channelId );
                    return;
                }

                final ChannelConfiguration cfg = new ChannelConfiguration ();
                try
                {
                    MetaKeys.bind ( cfg, channel.getMetaData () );
                }
                catch ( final Exception e )
                {
                    // do nothing
                }

                final String channelPath = toks.length > 1 ? toks[1] : null;

                if ( channelPath == null || channelPath.isEmpty () )
                {
                    if ( !request.getPathInfo ().endsWith ( "/" ) )
                    {
                        response.sendRedirect ( request.getRequestURI () + "/" );
                        return;
                    }

                    final Map<String, Object> model = new HashMap<> ();
                    model.put ( "dir", new IndexDirGenerator ( cfg ) );
                    Helper.render ( response, IndexDirGenerator.class.getResource ( "content/index.html" ), makeDefaultTitle ( channel ), model );
                    return;
                }

                if ( channelPath.equals ( "pool" ) )
                {
                    new PoolHandler ( channel, "", "" ).process ( response );
                    return;
                }

                final Matcher m = POOL_PATTERN.matcher ( channelPath );
                if ( m.matches () )
                {
                    new PoolHandler ( channel, m.group ( "aid" ), m.group ( "name" ) ).process ( response );
                    return;
                }

                if ( cfg == null || !cfg.isValid () )
                {
                    response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
                    response.getWriter ().format ( "APT configuration not found or not valid. Please ensure the 'APT Repository' aspect is added to this channel and the configuration is valid." );
                    return;
                }

                if ( channelPath.equals ( "dists" ) )
                {
                    if ( !request.getPathInfo ().endsWith ( "/" ) )
                    {
                        response.sendRedirect ( request.getRequestURI () + "/" );
                        return;
                    }

                    final Map<String, Object> model = new HashMap<> ();
                    model.put ( "distribution", cfg.getDistribution () );
                    model.put ( "id", channel.getId () );
                    Helper.render ( response, Helper.class.getResource ( "content/dists.html" ), makeDefaultTitle ( channel ), model );
                    return;
                }

                final Handler handler = makeHandler ( request, channel, channelPath, cfg );
                if ( handler == null )
                {
                    response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
                    response.getWriter ().format ( "Unable to handle request for '%s'", request.getPathInfo () );
                }
                else
                {
                    handler.process ( response );
                }

            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.getWriter ().format ( "Channel '%s' not found", channelId );
            return;
        }
    }

    protected String makeDefaultTitle ( final ReadableChannel channel )
    {
        return String.format ( "APT Repository | %s", channel.getInformation ().makeTitle () );
    }

    private Handler makeHandler ( final HttpServletRequest request, final ReadableChannel channel, final String channelPath, final ChannelConfiguration cfg )
    {
        final Map<String, Object> model = new HashMap<> ();
        model.put ( "distribution", cfg.getDistribution () );
        model.put ( "id", channel.getId ().getId () );

        final String toks[] = channelPath.split ( "/" );

        if ( toks.length == 1 && "GPG-KEY".equals ( toks[0] ) )
        {
            return new ChannelCacheHandler ( channel, new MetaKey ( AptChannelAspectFactory.ID, "GPG-KEY" ) );
        }

        if ( toks.length == 2 && "dists".equals ( toks[0] ) && cfg.getDistribution ().equals ( toks[1] ) )
        {
            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                return new RedirectHandler ( request );
            }

            model.put ( "dir", new DistDirGenerator ( cfg ) );
            return new ContentHandler ( DistDirGenerator.class.getResource ( "content/dist-index.html" ), makeDefaultTitle ( channel ), model );
        }

        if ( toks.length == 3 && "dists".equals ( toks[0] ) && cfg.getDistribution ().equals ( toks[1] ) )
        {
            final String component = toks[2];

            switch ( component )
            {
                case "InRelease":
                case "Release":
                case "Release.gpg":
                    return new ChannelCacheHandler ( channel, new MetaKey ( AptChannelAspectFactory.ID, String.format ( "dists/%s/%s", cfg.getDistribution (), component ) ) );
            }

            // TODO: handle all components when implemented

            if ( !cfg.getDefaultComponent ().contains ( component ) )
            {
                return null;
            }

            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                return new RedirectHandler ( request );
            }

            model.put ( "component", component );
            model.put ( "dir", new CompDirGenerator ( cfg ) );
            return new ContentHandler ( CompDirGenerator.class.getResource ( "content/comp-index.html" ), makeDefaultTitle ( channel ), model );
        }

        if ( toks.length == 4 && "dists".equals ( toks[0] ) && cfg.getDistribution ().equals ( toks[1] ) )
        {
            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                return new RedirectHandler ( request );
            }

            final String file = toks[3];
            if ( "source".equals ( file ) )
            {
                model.put ( "dir", new TypeDirGenerator ( cfg ) );
                return new ContentHandler ( TypeDirGenerator.class.getResource ( "content/type-index.html" ), makeDefaultTitle ( channel ), model );
            }

            for ( final String arch : cfg.getArchitectures () )
            {
                if ( file.equals ( "binary-" + arch ) )
                {
                    model.put ( "dir", new TypeDirGenerator ( cfg ) );
                    return new ContentHandler ( TypeDirGenerator.class.getResource ( "content/type-index.html" ), makeDefaultTitle ( channel ), model );
                }
            }
        }

        if ( toks.length == 5 && "dists".equals ( toks[0] ) && cfg.getDistribution ().equals ( toks[1] ) )
        {
            final String component = toks[2];
            final String type = toks[3];
            final String file = toks[4];
            switch ( file )
            {
                case "Release":
                case "Packages":
                    return new ChannelCacheHandler ( channel, new MetaKey ( AptChannelAspectFactory.ID, String.format ( "dists/%s/%s/%s/%s", cfg.getDistribution (), component, type, file ) ) );
                case "Packages.gz":
                    return new ChannelCacheHandler ( channel, new MetaKey ( AptChannelAspectFactory.ID, String.format ( "dists/%s/%s/%s/%s", cfg.getDistribution (), component, type, file ) ) );
                case "Packages.bz2":
                    return new ChannelCacheHandler ( channel, new MetaKey ( AptChannelAspectFactory.ID, String.format ( "dists/%s/%s/%s/%s", cfg.getDistribution (), component, type, file ) ) );
            }
        }

        return null;
    }
}
