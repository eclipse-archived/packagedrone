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
package org.eclipse.packagedrone.repo.manage.system.internal;

import java.io.FileReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.packagedrone.repo.manage.system.SystemService;

import com.google.common.io.CharStreams;

public class SystemServiceImpl implements SystemService
{
    private Server server;

    private final String hostname;

    public SystemServiceImpl ()
    {
        this.hostname = discoverHostname ();
    }

    public String getHostname ()
    {
        return this.hostname;
    }

    public void setServer ( final Server server )
    {
        this.server = server;
    }

    public void unsetServer ( final Server server )
    {
        if ( this.server == null )
        {
            this.server = null;
        }
    }

    @Override
    public String getDefaultSitePrefix ()
    {
        String prefix;

        prefix = System.getProperty ( "package.drone.site.prefix" );
        if ( prefix != null && !prefix.isEmpty () )
        {
            return prefix;
        }

        prefix = System.getenv ( "PACKAGE_DRONE_SITE_PREFIX" );
        if ( prefix != null && !prefix.isEmpty () )
        {
            return prefix;
        }

        prefix = makePrefixFromOsgiProperties ();
        if ( prefix != null )
        {
            return prefix;
        }

        return makePrefixFromJetty ();
    }

    /**
     * Make the prefix by guessing the port from the OSGi settings
     */
    protected String makePrefixFromOsgiProperties ()
    {
        final String port = System.getProperty ( "org.osgi.service.http.port" );
        if ( port == null )
        {
            return null;
        }

        final StringBuilder sb = new StringBuilder ();
        sb.append ( "http://" ).append ( discoverHostname () );
        if ( !"80".equals ( port ) )
        {
            sb.append ( ':' ).append ( port );
        }
        return sb.toString ();
    }

    /**
     * @deprecated this won't work with pax web
     */
    @Deprecated
    @SuppressWarnings ( "resource" )
    protected String makePrefixFromJetty ()
    {
        if ( this.server == null )
        {
            return null;
        }

        for ( final Connector c : this.server.getConnectors () )
        {
            if ( ! ( c instanceof NetworkConnector ) )
            {
                continue;
            }

            final NetworkConnector nc = (NetworkConnector)c;
            final StringBuilder sb = new StringBuilder ();

            String protocol = "http";

            final List<String> protos = nc.getProtocols ();
            for ( final String proto : protos )
            {
                // this seems to be that way jetty does it
                // http://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/tree/jetty-server/src/main/java/org/eclipse/jetty/server/Server.java
                if ( proto.startsWith ( "SSL-" ) )
                {
                    protocol = "https";
                    break;
                }
            }

            sb.append ( protocol ).append ( "://" );

            if ( nc.getHost () == null )
            {
                sb.append ( this.hostname );
            }
            else
            {
                sb.append ( nc.getHost () );
            }

            if ( "http".equals ( protocol ) && nc.getPort () == 80 )
            {
                // no port
            }
            else if ( "https".equals ( protocol ) && nc.getPort () == 443 )
            {
                // no port
            }
            else
            {
                sb.append ( ':' ).append ( nc.getPort () );
            }

            return sb.toString ();
        }
        return null;
    }

    private static String discoverHostname ()
    {
        String hostname = System.getenv ( "HOSTNAME" );

        if ( hostname == null )
        {
            hostname = System.getenv ( "COMPUTERNAME" );
        }

        if ( hostname == null )
        {
            try ( Reader reader = new FileReader ( "/etc/hostname" ) )
            {
                hostname = CharStreams.toString ( reader ).trim ();
            }
            catch ( final Exception e )
            {
            }
        }

        if ( hostname == null )
        {
            try
            {
                hostname = InetAddress.getLocalHost ().getHostName ();
            }
            catch ( final UnknownHostException e )
            {
            }
        }

        if ( hostname == null )
        {
            // last chance
            hostname = "localhost";
        }

        return hostname;
    }
}
