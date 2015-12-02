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

import static org.eclipse.packagedrone.testing.server.channel.CommonResources.RESOURCE_BASE;

import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.packagedrone.testing.server.AbstractServerTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DebTest extends AbstractServerTest
{
    private static final String DEB_RESOURCE = RESOURCE_BASE + "deb/org.eclipse.scada_0.2.1_all.deb";

    @Test
    public void testDeb1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "deb1" );
        ct.addAspect ( "deb" );
        ct.addAspect ( "apt" );

        {
            final Set<String> result = ct.upload ( DEB_RESOURCE );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 1, ct.getAllArtifactIds ().size () );

        testUrl ( String.format ( "/apt/%s", ct.getId () ), Pattern.compile ( ".*APT Repository \\|.*", Pattern.DOTALL ) ); // index page
    }

    @Test
    public void testDeb2 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "deb2" );
        ct.addAspect ( "deb" );
        ct.addAspect ( "apt" );

        {
            final Set<String> result = ct.upload ( DEB_RESOURCE );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 1, ct.getAllArtifactIds ().size () );

        testUrl ( String.format ( "/apt/%s", ct.getId () ) ); // index page

        getWebContext ().getResolved ( String.format ( "/config/deb/channel/%s/edit", ct.getId () ) );

        final WebElement desc = getWebContext ().findElement ( By.name ( "description" ) );
        desc.sendKeys ( "Test Description" );
        desc.submit ();

        testUrl ( String.format ( "/apt/%s", ct.getId () ), Pattern.compile ( ".*APT Repository \\|.*", Pattern.DOTALL ) ); // index page

        testUrl ( String.format ( "/apt/%s/dists/default/Release", ct.getId () ) );
        testUrl ( String.format ( "/apt/%s/dists/default/main/binary-amd64/Release", ct.getId () ) );
        testUrl ( String.format ( "/apt/%s/dists/default/main/binary-amd64/Packages", ct.getId () ) );
        testUrl ( String.format ( "/apt/%s/dists/default/main/binary-i386/Release", ct.getId () ) );
        testUrl ( String.format ( "/apt/%s/dists/default/main/binary-i386/Packages", ct.getId () ) );
    }

}
