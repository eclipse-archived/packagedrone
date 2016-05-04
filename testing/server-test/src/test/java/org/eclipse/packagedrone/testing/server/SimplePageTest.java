/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.testing.server;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SimplePageTest extends AbstractServerTest
{

    @Test
    public void testBase ()
    {
        simpleTest ( "/" );
    }

    @Test
    public void testAbout ()
    {
        simpleTest ( "/about", () -> {
            final WebElement buildIdEle = getWebContext ().findElement ( By.className ( "about-build-id" ) );
            final String text = buildIdEle.getText ();

            Assert.assertNotNull ( text );
            Assert.assertTrue ( Pattern.matches ( "[0-9]{8}-[0-9]{4}", text ) );
        } );
    }

    @Test
    public void testChannels ()
    {
        simpleTest ( "/channel" );
    }

    @Test
    public void testMailConfig ()
    {
        simpleTest ( "/default.mail/config" );
    }

    @Test
    public void testSignConfig ()
    {
        simpleTest ( "/pgp.sign" );
    }

    @Test
    public void testDeployKeys ()
    {
        simpleTest ( "/deploy/auth/group" );
    }

    @Test
    public void testCoreView ()
    {
        simpleTest ( "/config/core/list" );
    }

    @Test
    public void testCoreSite ()
    {
        simpleTest ( "/config/core/site" );
    }

    @Test
    public void testTasks ()
    {
        simpleTest ( "/tasks" );
    }

    @Test
    public void testSystemBackup ()
    {
        simpleTest ( "/system/backup" );
    }

    @Test
    public void testSystemInfo ()
    {
        simpleTest ( "/system/info" );
    }

    @Test
    public void testSystemAnalytics ()
    {
        simpleTest ( "/system/extend/analytics" );
    }

    protected void simpleTest ( final String url )
    {
        simpleTest ( url, null );
    }

    protected void simpleTest ( final String url, final Runnable additionalChecks )
    {
        final String full = resolve ( url );
        driver.get ( full );
        Assert.assertEquals ( full, driver.getCurrentUrl () );

        final String title = driver.getTitle ();
        System.out.println ( "Page title: " + title );
        Assert.assertTrue ( "Page title suffix", title.contains ( "| Eclipse Package Drone" ) );

        if ( additionalChecks != null )
        {
            additionalChecks.run ();
        }
    }
}
