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
package org.eclipse.packagedrone.repo.importer.aether;

import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.DefaultServiceLocator.ErrorHandler;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.packagedrone.VersionInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helper
{
    private final static Logger logger = LoggerFactory.getLogger ( Helper.class );

    public static RepositorySystem newRepositorySystem ()
    {
        final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator ();

        locator.addService ( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService ( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService ( TransporterFactory.class, HttpTransporterFactory.class );

        locator.setErrorHandler ( new ErrorHandler () {
            @Override
            public void serviceCreationFailed ( final Class<?> type, final Class<?> impl, final Throwable exception )
            {
                final Logger logger = LoggerFactory.getLogger ( impl );
                logger.warn ( "Service creation failed: " + type.getName (), exception );
            }
        } );

        return locator.getService ( RepositorySystem.class );
    }

    public static DefaultRepositorySystemSession newRepositorySystemSession ( final Path tempDir, final RepositorySystem system )
    {
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession ();

        final LocalRepository localRepo = new LocalRepository ( tempDir.toFile () );
        session.setLocalRepositoryManager ( system.newLocalRepositoryManager ( session, localRepo ) );

        session.setTransferListener ( new LoggerTransferListener () );
        session.setConfigProperty ( ConfigurationProperties.USER_AGENT, VersionInformation.USER_AGENT );

        return session;
    }

    public static RemoteRepository newCentralRepository ()
    {
        return newRemoteRepository ( "central", System.getProperty ( "drone.importer.aether.central.url", "http://central.maven.org/maven2/" ) );
    }

    public static RemoteRepository newRemoteRepository ( final String id, final String url )
    {
        final RemoteRepository.Builder builder = new RemoteRepository.Builder ( id, "default", url );

        builder.setProxy ( getProxy ( url ) );

        return builder.build ();
    }

    private static Proxy getProxy ( final String url )
    {
        final ProxySelector ps = ProxySelector.getDefault ();
        if ( ps == null )
        {
            logger.debug ( "No proxy selector found" );
            return null;
        }

        final List<java.net.Proxy> proxies = ps.select ( URI.create ( url ) );
        for ( final java.net.Proxy proxy : proxies )
        {
            if ( proxy.type () != Type.HTTP )
            {
                logger.debug ( "Unsupported proxy type: {}", proxy.type () );
                continue;
            }

            final SocketAddress addr = proxy.address ();
            logger.debug ( "Proxy address: {}", addr );

            if ( ! ( addr instanceof InetSocketAddress ) )
            {
                logger.debug ( "Unsupported proxy address type: {}", addr.getClass () );
                continue;
            }

            final InetSocketAddress inetAddr = (InetSocketAddress)addr;

            return new Proxy ( Proxy.TYPE_HTTP, inetAddr.getHostString (), inetAddr.getPort () );
        }

        logger.debug ( "No proxy found" );
        return null;
    }
}
