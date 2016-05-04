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
package org.eclipse.packagedrone.testing.server;

import org.junit.Assert;
import org.junit.Test;

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
        simpleTest ( "/about" );
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
        final String full = resolve ( url );
        driver.get ( full );
        Assert.assertEquals ( full, driver.getCurrentUrl () );

        final String title = driver.getTitle ();
        System.out.println ( "Page title: " + title );
        Assert.assertTrue ( "Page title suffix", title.contains ( "| Eclipse Package Drone" ) );
    }
}
