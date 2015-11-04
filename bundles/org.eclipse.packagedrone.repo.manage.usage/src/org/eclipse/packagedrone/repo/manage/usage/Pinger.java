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
package org.eclipse.packagedrone.repo.manage.usage;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.eclipse.packagedrone.VersionInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Pinger extends Thread
{
    private final static Logger logger = LoggerFactory.getLogger ( Pinger.class );

    private final static String PING_URL = "http://packagedrone.org/stats/";

    private final static URL URL;

    static
    {
        URL url = null;
        try
        {
            url = new URL ( PING_URL );
        }
        catch ( final MalformedURLException e )
        {
        }
        URL = url;
    }

    private final Statistics statistics;

    public Pinger ( final Statistics statistics )
    {
        this.statistics = statistics;

        setDaemon ( true );
        setName ( "Pinger" );
        setPriority ( Thread.MIN_PRIORITY );
    }

    @Override
    public void run ()
    {
        if ( URL == null )
        {
            logger.info ( "Trouble parsing ping URL: {}", PING_URL );
            return;
        }

        final long start = System.currentTimeMillis ();
        logger.debug ( "Starting ping" );
        try
        {
            performPing ( this.statistics );
        }
        catch ( final Exception e )
        {
            logger.info ( "Failed to ping", e );
        }
        finally
        {
            logger.debug ( "Ended ping after {} ms", System.currentTimeMillis () - start );
        }
    }

    protected static void performPing ( final Statistics statistics ) throws Exception
    {
        final URLConnection con = URL.openConnection ();

        con.setDoOutput ( true );
        con.setUseCaches ( false );

        logger.debug ( "Connection: {}", con );

        if ( con instanceof HttpURLConnection )
        {
            ( (HttpURLConnection)con ).setRequestMethod ( "POST" );
            ( (HttpURLConnection)con ).setInstanceFollowRedirects ( false );
        }

        con.setRequestProperty ( "Content-type", "text/json" );
        con.setRequestProperty ( "User-agent", VersionInformation.USER_AGENT );

        try ( OutputStream out = con.getOutputStream () )
        {
            final GsonBuilder gb = new GsonBuilder ();
            final Gson g = gb.create ();
            final OutputStreamWriter writer = new OutputStreamWriter ( out, StandardCharsets.UTF_8 );
            g.toJson ( statistics, writer );
            writer.flush ();
        }

        try ( InputStream in = con.getInputStream () )
        {
            final String result = CharStreams.toString ( new InputStreamReader ( in, StandardCharsets.UTF_8 ) );
            logger.debug ( "Ping result: {}", result );
        }
    }

}
