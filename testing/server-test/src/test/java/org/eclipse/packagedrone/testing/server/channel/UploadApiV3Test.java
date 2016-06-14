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
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.api.transfer.ContentProvider;
import org.eclipse.packagedrone.repo.api.transfer.TransferArchiveWriter;
import org.eclipse.packagedrone.testing.server.AbstractServerTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.CharStreams;

public class UploadApiV3Test extends AbstractServerTest
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
            this.tester = ChannelTester.create ( getWebContext (), "uploadapi2" );
            this.tester.assignDeployGroup ( "m1" );
            this.deployKey = this.tester.getDeployKeys ().iterator ().next ();
        }
        return this.tester;
    }

    @Test
    public void upload1Plain () throws URISyntaxException, IOException
    {
        final ChannelTester tester = getTester ();

        final File file = getAbsolutePath ( CommonResources.BUNDLE_1_RESOURCE );

        final URIBuilder b = new URIBuilder ( resolve ( "/api/v3/upload/plain/channel/%s/%s", tester.getId (), file.getName () ) );
        b.setUserInfo ( "deploy", this.deployKey );
        b.addParameter ( "foo:bar", "baz" );

        try ( final CloseableHttpResponse response = upload ( b, file ) )
        {
            final String result = CharStreams.toString ( new InputStreamReader ( response.getEntity ().getContent (), StandardCharsets.UTF_8 ) );
            System.out.println ( "Result: " + response.getStatusLine () );
            System.out.println ( result );
            Assert.assertEquals ( 200, response.getStatusLine ().getStatusCode () );
        }

        final Set<String> arts = tester.getAllArtifactIds ();

        Assert.assertEquals ( 1, arts.size () );
    }

    @Test
    public void upload2Archive () throws URISyntaxException, IOException
    {
        final ChannelTester tester = getTester ();

        final File file1 = getAbsolutePath ( CommonResources.BUNDLE_1_RESOURCE );
        final File file2 = getAbsolutePath ( CommonResources.BUNDLE_2_RESOURCE );

        final Path tmp = Files.createTempFile ( "upload", null );
        try
        {
            try ( TransferArchiveWriter writer = new TransferArchiveWriter ( Files.newOutputStream ( tmp ) ) )
            {
                final Map<MetaKey, String> properties = new HashMap<> ();
                properties.put ( new MetaKey ( "foo", "bar" ), "baz" );
                properties.put ( new MetaKey ( "foo", "bar2" ), "baz2" );

                writer.createEntry ( file1.getName (), properties, ContentProvider.file ( file1 ) );
                writer.createEntry ( file2.getName (), properties, ContentProvider.file ( file2 ) );
            }

            final URIBuilder b = new URIBuilder ( resolve ( "/api/v3/upload/archive/channel/%s", tester.getId () ) );
            b.setUserInfo ( "deploy", this.deployKey );

            try ( final CloseableHttpResponse response = upload ( b, tmp.toFile () ) )
            {
                final String result = CharStreams.toString ( new InputStreamReader ( response.getEntity ().getContent (), StandardCharsets.UTF_8 ) );
                System.out.println ( "Result: " + response.getStatusLine () );
                System.out.println ( result );
                Assert.assertEquals ( 200, response.getStatusLine ().getStatusCode () );
            }

            final Set<String> arts = tester.getAllArtifactIds ();

            Assert.assertEquals ( 2, arts.size () );
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    protected CloseableHttpResponse upload ( final URIBuilder uri, final File file ) throws IOException, URISyntaxException
    {
        final HttpPut httppost = new HttpPut ( uri.build () );
        httppost.setEntity ( new FileEntity ( file ) );

        return httpclient.execute ( httppost );
    }
}
