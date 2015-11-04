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
package org.eclipse.packagedrone.repo.importer.http;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ParseException;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.importer.ImportContext;
import org.eclipse.packagedrone.repo.importer.Importer;
import org.eclipse.packagedrone.repo.importer.ImporterDescription;
import org.eclipse.packagedrone.repo.importer.SimpleImporterDescription;
import org.eclipse.packagedrone.utils.Strings;
import org.eclipse.packagedrone.web.LinkTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpImporter implements Importer
{
    private final static Logger logger = LoggerFactory.getLogger ( HttpImporter.class );

    public static final String ID = "http";

    private static final SimpleImporterDescription DESCRIPTION;

    private static final int COPY_BUFFER_SIZE = Integer.getInteger ( "drone.importer.http.copyBufferSize", 4096 * 10 );

    private final Gson gson;

    public HttpImporter ()
    {
        this.gson = new GsonBuilder ().create ();
    }

    static
    {
        DESCRIPTION = new SimpleImporterDescription ();
        DESCRIPTION.setId ( ID );
        DESCRIPTION.setLabel ( "HTTP Importer" );
        DESCRIPTION.setDescription ( "Import artifacts by downloading the provided URL" );
        DESCRIPTION.setStartTarget ( new LinkTarget ( "/import/{token}/http/start" ) );
    }

    @Override
    public ImporterDescription getDescription ()
    {
        return DESCRIPTION;
    }

    @Override
    public void runImport ( final ImportContext context, final String configuration ) throws Exception
    {
        final Configuration cfg = this.gson.fromJson ( configuration, Configuration.class );

        logger.debug ( "Get URL: {}", cfg.getUrl () );

        final URL url = new URL ( cfg.getUrl () );

        final Path file = Files.createTempFile ( "import", null );

        final URLConnection con = url.openConnection ();

        con.setRequestProperty ( "User-Agent", VersionInformation.USER_AGENT );

        String name;

        final Context job = context.getJobContext ();

        try ( final InputStream in = con.getInputStream ();
              OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
        {
            final long length = con.getContentLengthLong ();

            if ( length > 0 )
            {
                job.beginWork ( String.format ( "Downloading %s", Strings.bytes ( length ) ), length );
            }

            // manual copy
            final byte[] buffer = new byte[COPY_BUFFER_SIZE];
            int rc;
            while ( ( rc = in.read ( buffer ) ) > 0 )
            {
                out.write ( buffer, 0, rc );
                job.worked ( rc );
            }

            job.complete ();

            // get the name inside here, since this will properly clean up if something fails

            name = makeName ( cfg, url, con );
            if ( name == null )
            {
                throw new IllegalStateException ( String.format ( "Unable to determine name for %s", cfg.getUrl () ) );
            }
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to download", e );
            Files.deleteIfExists ( file );
            throw e;
        }

        context.scheduleImport ( file, name );
    }

    public static String makeName ( final Configuration cfg, final URL url, final URLConnection con )
    {
        String name = cfg.getAlternateName ();

        if ( name == null || name.isEmpty () )
        {
            name = fromContentDisposition ( con.getHeaderField ( "Content-Disposition" ) );
        }
        if ( name == null || name.isEmpty () )
        {
            name = fromPath ( url.getPath () );
        }
        return name;
    }

    private static String fromContentDisposition ( final String field )
    {
        if ( field == null || field.isEmpty () )
        {
            return null;
        }

        try
        {
            final ContentDisposition cd = new ContentDisposition ( field );
            return cd.getParameter ( "filename" );
        }
        catch ( final ParseException e )
        {
            return null;
        }
    }

    private static String fromPath ( final String path )
    {
        if ( path == null )
        {
            return null;
        }

        final String[] segs = path.split ( "/" );

        if ( segs.length == 0 )
        {
            return null;
        }

        return segs[segs.length - 1];
    }
}
