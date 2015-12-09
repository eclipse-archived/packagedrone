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

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
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

        final Path javaBin = Paths.get ( javaHome, "bin", "java" );

        final ProcessBuilder pb = new ProcessBuilder ( javaBin.toAbsolutePath ().toString () );

        final Map<String, String> additional = new HashMap<> ();
        makeProcessSystemProperties ( pb, additional );

        try
        {
            Files.getPosixFilePermissions ( javaBin );
        }
        catch ( final UnsupportedOperationException e )
        {
            pb.command ().add ( "-Dpackage.drone.admin.announce.file.notPosix=true" );
        }

        pb.command ().add ( "-jar" );
        pb.command ().add ( findLauncher () );
        pb.command ().add ( "-consoleLog" );

        pb.inheritIO ();

        new StringJoiner ( ", " );
        this.log.format ( "Starting server: %s%n", pb.command ().stream ().collect ( joining ( " " ) ) );
        this.log.flush ();

        this.process = pb.start ();

        this.log.format ( "Started ... %s%n", this.process );
        this.log.flush ();

        waitForServerUp ();

        this.log.println ( "Port open" );
        Thread.sleep ( 1_000 );
    }

    private String findLauncher () throws IOException
    {
        final Path base = Paths.get ( "target", "instance", "plugins" );
        return Files.walk ( base ).filter ( p -> {
            final String s = p.getFileName ().toString ();
            return s.startsWith ( "org.eclipse.equinox.launcher_" ) && s.endsWith ( ".jar" );
        } ).findAny ().orElseThrow ( () -> new IllegalStateException ( "Unable to find equinox launcher" ) ).toAbsolutePath ().toString ();
    }

    private void waitForServerUp () throws InterruptedException
    {
        final Instant start = Instant.now ();
        int i = 1;
        while ( !isServerUp () )
        {
            this.log.format ( "\tTest for server #%s%n", i );
            this.log.flush ();
            if ( Duration.between ( start, Instant.now () ).compareTo ( START_TIMEOUT ) > 0 )
            {
                this.process.destroyForcibly ();
                throw new IllegalStateException ( "Failed to wait for port" );
            }

            Thread.sleep ( 1_000 );
            i++;
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
                pb.command ().add ( String.format ( "-D%s=%s", key, value ) );
            }
        }

        for ( final Map.Entry<String, String> entry : additional.entrySet () )
        {
            final String key = entry.getKey ();
            final String value = entry.getValue ();

            pb.command ().add ( String.format ( "-D%s=%s", key, value ) );
        }
    }

    private boolean isServerUp ()
    {
        try
        {
            final URL url = new URL ( "http://localhost:" + this.port );

            final HttpURLConnection con = (HttpURLConnection)url.openConnection ();
            con.connect ();
            try
            {
                return con.getResponseCode () == 200;
            }
            finally
            {
                con.disconnect ();
            }
        }
        catch ( final Exception e )
        {
            return false;
        }
    }

}
