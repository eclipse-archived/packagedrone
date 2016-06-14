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
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHeaders;
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

    @Test
    public void upload1Plain () throws URISyntaxException, IOException
    {
        final ChannelTester tester = ChannelTester.create ( getWebContext (), "uploadapi2a" );
        tester.assignDeployGroup ( "m1" );
        final String deployKey = tester.getDeployKeys ().iterator ().next ();

        final File file = getAbsolutePath ( CommonResources.BUNDLE_1_RESOURCE );

        final URIBuilder b = new URIBuilder ( resolve ( "/api/v3/upload/plain/channel/%s/%s", tester.getId (), file.getName () ) );
        b.setUserInfo ( "deploy", deployKey );
        b.addParameter ( "foo:bar", "baz" );

        System.out.println ( "Request: " + b.build () );

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
        final ChannelTester tester = ChannelTester.create ( getWebContext (), "uploadapi2b" );
        tester.assignDeployGroup ( "m1" );
        final String deployKey = tester.getDeployKeys ().iterator ().next ();

        final File file1 = getAbsolutePath ( CommonResources.BUNDLE_1_RESOURCE );
        final File file2 = getAbsolutePath ( CommonResources.BUNDLE_2_RESOURCE );

        final Path tmp = Paths.get ( "upload-1.zip" );

        try ( TransferArchiveWriter writer = new TransferArchiveWriter ( Files.newOutputStream ( tmp ) ) )
        {
            final Map<MetaKey, String> properties = new HashMap<> ();
            properties.put ( new MetaKey ( "foo", "bar" ), "baz" );
            properties.put ( new MetaKey ( "foo", "bar2" ), "baz2" );

            writer.createEntry ( file1.getName (), properties, ContentProvider.file ( file1 ) );
            writer.createEntry ( file2.getName (), properties, ContentProvider.file ( file2 ) );
        }

        final URIBuilder b = new URIBuilder ( resolve ( "/api/v3/upload/archive/channel/%s", tester.getId () ) );
        b.setUserInfo ( "deploy", deployKey );

        System.out.println ( "Request: " + b.build () );

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

    protected CloseableHttpResponse upload ( final URIBuilder uri, final File file ) throws IOException, URISyntaxException
    {
        final HttpPut http = new HttpPut ( uri.build () );

        final String encodedAuth = Base64.getEncoder ().encodeToString ( uri.getUserInfo ().getBytes ( StandardCharsets.ISO_8859_1 ) );

        http.setHeader ( HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth );

        http.setEntity ( new FileEntity ( file ) );
        return httpclient.execute ( http );
    }
}
