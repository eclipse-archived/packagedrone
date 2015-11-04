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
package org.eclipse.packagedrone.repo.importer.http.web;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.job.AbstractJsonJobFactory;
import org.eclipse.packagedrone.job.JobFactoryDescriptor;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.importer.http.Configuration;
import org.eclipse.packagedrone.repo.importer.http.HttpImporter;
import org.eclipse.packagedrone.web.LinkTarget;

public class DownloadTester extends AbstractJsonJobFactory<Configuration, TestResult>
{
    public static final String ID = "org.eclipse.packagedrone.repo.importer.http.web.tester";

    private static final JobFactoryDescriptor DESCRIPTOR = new JobFactoryDescriptor () {

        @Override
        public LinkTarget getResultTarget ()
        {
            return null;
        }
    };

    public DownloadTester ()
    {
        super ( Configuration.class );
    }

    @Override
    protected TestResult process ( final Context context, final Configuration cfg ) throws Exception
    {
        final TestResult result = new TestResult ();

        final URL url = new URL ( cfg.getUrl () );
        final URLConnection con = url.openConnection ();
        con.setRequestProperty ( "User-Agent", VersionInformation.USER_AGENT );

        if ( con instanceof HttpURLConnection )
        {
            final HttpURLConnection httpCon = (HttpURLConnection)con;
            httpCon.setRequestMethod ( "HEAD" );
        }

        con.connect ();

        if ( con instanceof HttpURLConnection )
        {
            final HttpURLConnection httpCon = (HttpURLConnection)con;
            result.setReturnCode ( httpCon.getResponseCode () );
            final long length = httpCon.getContentLengthLong ();
            result.setContentLength ( length );
        }

        final String name = HttpImporter.makeName ( cfg, url, con );
        result.setName ( name );

        return result;
    }

    @Override
    protected String makeLabelFromData ( final Configuration data )
    {
        return String.format ( "Test download from: %s", data.getUrl () );
    }

    @Override
    public JobFactoryDescriptor getDescriptor ()
    {
        return DESCRIPTOR;
    }
}
