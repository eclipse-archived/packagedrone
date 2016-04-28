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
package org.eclipse.packagedrone.testing.server.channel;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.packagedrone.testing.server.AbstractServerTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class UploadTest extends AbstractServerTest
{
    private static final String ROW_ID_PREFIX = "row-";

    @Test
    public void test () throws Exception
    {
        driver.get ( resolve ( "/channel" ) );

        System.out.println ( "Before" );

        Assert.assertTrue ( getChannels ().isEmpty () );

        driver.get ( resolve ( "/channel/create" ) );

        final Collection<String> channels = getChannels ();
        Assert.assertEquals ( 1, channels.size () );

        final String channel = channels.iterator ().next ();

        withChannel ( channel );
        basicWithChannel ( channel );
    }

    private void basicWithChannel ( final String channelId ) throws Exception
    {
        testUrl ( "/channel/" + channelId + "/description" );
        testUrl ( "/channel/" + channelId + "/help.maven" );
    }

    @Override
    protected void testUrl ( final String suffix ) throws Exception
    {
        final URL url = new URL ( resolve ( suffix ) );
        try ( InputStream is = url.openStream () )
        {
        }
    }

    private void withChannel ( final String channelId )
    {
        driver.get ( resolve ( String.format ( "/channel/%s/viewPlain", channelId ) ) );

        // should be an empty channel

        Assert.assertTrue ( findArtifacts ().isEmpty () );

        driver.get ( resolve ( String.format ( "/channel/%s/add", channelId ) ) );

        // test for "Upload" active

        final WebElement link = driver.findElement ( By.linkText ( "Upload" ) );
        Assert.assertTrue ( link.findElement ( By.xpath ( ".." ) ).getAttribute ( "class" ).contains ( "active" ) );

        // upload file

        final File input = getAbsolutePath ( CommonResources.BUNDLE_1_RESOURCE );

        {
            final WebElement ele = driver.findElementById ( "file" );
            Assert.assertNotNull ( ele );

            ele.sendKeys ( input.toString () );

            ele.submit ();
        }

        // navigate to plain view

        driver.get ( resolve ( String.format ( "/channel/%s/viewPlain", channelId ) ) );

        // check upload

        final List<String> arts = findArtifacts ();
        Assert.assertEquals ( 1, arts.size () );
    }

    protected Set<String> getChannels ()
    {
        final Set<String> result = new HashSet<> ();

        for ( final WebElement ele : driver.findElementsByCssSelector ( "#channels tr" ) )
        {
            if ( ele.getAttribute ( "data-channel-id" ) != null )
            {
                result.add ( ele.getAttribute ( "data-channel-id" ) );
            }
        }

        return result;
    }

    protected List<String> findArtifacts ()
    {
        final List<String> result = new LinkedList<> ();

        for ( final WebElement ele : driver.findElementsByTagName ( "tr" ) )
        {
            final String id = ele.getAttribute ( "id" );
            System.out.format ( "Entry: %s%n", id );
            if ( id == null || !id.startsWith ( ROW_ID_PREFIX ) )
            {
                continue;
            }

            result.add ( id.substring ( ROW_ID_PREFIX.length () ) );
        }

        return result;
    }
}
