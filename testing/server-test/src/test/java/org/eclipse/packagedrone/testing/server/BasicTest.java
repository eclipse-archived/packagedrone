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

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

public class BasicTest extends AbstractServerTest
{
    @Test
    public void testMain () throws Exception
    {
        testUrl ( "/" );
    }

    @Test
    public void testSitemap () throws Exception
    {
        testUrl ( "/sitemap.xml" );
    }

    @Test
    public void testP2 () throws Exception
    {
        testUrl ( "/p2" );
    }

    @Test
    public void testR5 () throws Exception
    {
        testUrl ( "/r5" );
    }

    @Test
    public void testNpm () throws Exception
    {
        testUrl ( "/npm" );
    }

    @Test
    public void testApt () throws Exception
    {
        testUrl ( "/apt" );
    }

    @Test
    public void testYum () throws Exception
    {
        testUrl ( "/yum" );
    }

    @Test
    public void testMaven () throws Exception
    {
        testUrl ( "/maven" );
    }

    @Test
    public void testUser () throws Exception
    {
        testNotAuth ( "/user" );
    }

    @Test
    public void testConfig () throws Exception
    {
        testNotAuth ( "/config" );
    }

    @Test
    public void testConfigCoreList () throws Exception
    {
        testNotAuth ( "/config/core/list" );
    }

    @Test
    public void testConfigCoreSite () throws Exception
    {
        testNotAuth ( "/config/core/site" );
    }

    @Test
    public void testMailConfig () throws Exception
    {
        testNotAuth ( "/default.mail/config" );
    }

    protected void testNotAuth ( final String suffix ) throws Exception
    {
        final URL url = new URL ( resolve ( suffix ) );

        final HttpURLConnection con = (HttpURLConnection)url.openConnection ();
        con.connect ();
        con.getResponseCode ();
        con.disconnect ();
    }

}
