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
package org.eclipse.packagedrone.testing.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerRunner
{
    private static final Duration START_TIMEOUT = Duration.ofSeconds ( 30 );

    private final int port;

    private Process process;

    private final PrintStream log;

    public ServerRunner ( final int port )
    {
        this.port = port;
        this.log = System.out;
    }

    public void start () throws InterruptedException, IOException
    {
        final String javaHome = System.getProperty ( "java.home" );

        final Path path = Paths.get ( "target", "instance", "server" );

        try
        {
            Files.setPosixFilePermissions ( path, EnumSet.of ( PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ ) );
        }
        catch ( final UnsupportedOperationException e )
        {
            // ignore
        }

        final ProcessBuilder pb = new ProcessBuilder ( "target/instance/server" );

        pb.environment ().put ( "JAVA_HOME", javaHome );

        final Map<String, String> additional = new HashMap<> ();
        makeProcessSystemProperties ( pb, additional );

        pb.inheritIO ();

        this.log.format ( "Starting server: %s%n", pb );
        this.log.flush ();

        this.process = pb.start ();

        this.log.format ( "Started ... %s%n", this.process );
        this.log.flush ();

        waitForPortOpen ();

        this.log.println ( "Port open" );
        Thread.sleep ( 1_000 );
    }

    private void waitForPortOpen () throws InterruptedException
    {
        final Instant start = Instant.now ();
        while ( !isPortOpen () )
        {
            if ( Duration.between ( start, Instant.now () ).compareTo ( START_TIMEOUT ) > 0 )
            {
                this.process.destroyForcibly ();
                throw new IllegalStateException ( "Failed to wait for port" );
            }
            Thread.sleep ( 1_000 );
        }
    }

    public void stop () throws InterruptedException
    {
        this.log.print ( "Stopping server..." );
        this.log.flush ();

        if ( this.process != null )
        {
            if ( !this.process.destroyForcibly ().waitFor ( 10, TimeUnit.SECONDS ) )
            {
                throw new IllegalStateException ( "Failed to terminate process" );
            }
        }

        this.log.println ( "stopped!" );
    }

    private static void makeProcessSystemProperties ( final ProcessBuilder pb, final Map<String, String> additional )
    {
        final StringBuilder sb = new StringBuilder ();
        for ( final Map.Entry<Object, Object> entry : System.getProperties ().entrySet () )
        {
            if ( entry.getKey () == null || entry.getValue () == null )
            {
                continue;
            }

            final String key = entry.getKey ().toString ();
            final String value = entry.getValue ().toString ();

            if ( key.startsWith ( "org.osgi." ) || key.startsWith ( "drone." ) )
            {
                if ( sb.length () > 0 )
                {
                    sb.append ( ' ' );
                }
                sb.append ( "-D" ).append ( key ).append ( '=' ).append ( value );
            }
        }

        for ( final Map.Entry<String, String> entry : additional.entrySet () )
        {
            final String key = entry.getKey ();
            final String value = entry.getValue ();

            if ( sb.length () > 0 )
            {
                sb.append ( ' ' );
            }
            sb.append ( "-D" ).append ( key ).append ( '=' ).append ( value );
        }

        pb.environment ().put ( "JAVA_OPTS", sb.toString () );
    }

    private boolean isPortOpen ()
    {
        try ( ServerSocket server = new ServerSocket ( this.port ) )
        {
            // there is a slim chance that by doing this, we actually block the other process opening this port
            return false;
        }
        catch ( final IOException e1 )
        {
            return true;
        }
    }

}
