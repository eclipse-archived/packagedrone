/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.testing.server.channel;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.packagedrone.testing.server.AbstractServerTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class UploadApiTest extends AbstractServerTest
{
    private static CloseableHttpClient httpclient;

    @BeforeClass
    public static void setup ()
    {
        httpclient = HttpClientBuilder.create ().build ();
    }

    @AfterClass
    public static void destroy () throws IOException
    {
        httpclient.close ();
    }

    private ChannelTester tester;

    private String deployKey;

    protected ChannelTester getTester ()
    {
        if ( this.tester == null )
        {
            this.tester = ChannelTester.create ( getWebContext (), "uploadapi1" );
            this.tester.assignDeployGroup ( "m1" );
            this.deployKey = this.tester.getDeployKeys ().iterator ().next ();
        }
        return this.tester;
    }

    @Test
    public void upload1 () throws URISyntaxException, IOException
    {
        final ChannelTester tester = getTester ();

        final File file = getAbsolutePath ( CommonResources.BUNDLE_1_RESOURCE );

        final URIBuilder b = new URIBuilder ( resolve ( "/api/v2/upload/channel/%s/%s", tester.getId (), file.getName () ) );
        b.setUserInfo ( "deploy", this.deployKey );
        b.addParameter ( "foo:bar", "baz" );

        try ( final CloseableHttpResponse response = upload ( b, file ) )
        {
            Assert.assertEquals ( 200, response.getStatusLine ().getStatusCode () );
        }

        final Set<String> arts = tester.getAllArtifactIds ();

        Assert.assertEquals ( 1, arts.size () );
    }

    protected CloseableHttpResponse upload ( final URIBuilder uri, final File file ) throws IOException, URISyntaxException
    {
        final HttpPut httppost = new HttpPut ( uri.build () );
        httppost.setEntity ( new FileEntity ( file ) );

        return httpclient.execute ( httppost );
    }
}
