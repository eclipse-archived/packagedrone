/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.testing.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.packagedrone.testing.server.channel.DebTest;
import org.eclipse.packagedrone.testing.server.channel.MavenTest;
import org.eclipse.packagedrone.testing.server.channel.MvnOsgiTest;
import org.eclipse.packagedrone.testing.server.channel.OsgiTest;
import org.eclipse.packagedrone.testing.server.channel.P2Test;
import org.eclipse.packagedrone.testing.server.channel.RpmTest;
import org.eclipse.packagedrone.testing.server.channel.UploadApiV2Test;
import org.eclipse.packagedrone.testing.server.channel.UploadApiV3Test;
import org.eclipse.packagedrone.testing.server.channel.UploadTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.Platform;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

@RunWith ( Suite.class )
@SuiteClasses ( { //
        BasicTest.class, //
        UserTest.class, // now we are logged in
        SimplePageTest.class, //
        MailTest.class, //
        DeployGroupTest.class, //
        UploadTest.class, //
        P2Test.class, //
        DebTest.class, //
        RpmTest.class, //
        OsgiTest.class, //
        MvnOsgiTest.class, //
        MavenTest.class, //
        UploadApiV2Test.class, //
        UploadApiV3Test.class //
} )
public class TestSuite
{
    public static final int TEST_PORT = Integer.getInteger ( "org.osgi.service.http.port", 8080 );

    private final static ServerRunner server = new ServerRunner ( TEST_PORT );

    private static final String SAUCE_USER_NAME = System.getenv ( "SAUCE_USERNAME" );

    private static final String SAUCE_ACCESS_KEY = System.getenv ( "SAUCE_ACCESS_KEY" );

    private static final String SAUCE_URL = System.getenv ().getOrDefault ( "SAUCE_URL", "localhost:4445" );

    private static final String SAUCE_PLATFORM = System.getProperty ( "sauce.platform", "win10" );

    private static final String SAUCE_BROWSER = System.getProperty ( "sauce.browser", "chrome" );

    private static RemoteWebDriver driver;

    private static Client client;

    public static RemoteWebDriver getDriver ()
    {
        return driver;
    }

    @BeforeClass
    public static void startServer () throws IOException, InterruptedException
    {
        server.start ();
    }

    @AfterClass
    public static void stopServer () throws InterruptedException
    {
        if ( server != null )
        {
            server.stop ();
        }
    }

    @BeforeClass
    public static void startBrowser () throws MalformedURLException
    {
        if ( SAUCE_USER_NAME != null )
        {
            driver = createSauce ( Platform.fromString ( SAUCE_PLATFORM ), SAUCE_BROWSER, null );
        }
        else
        {
            // driver = new MarionetteDriver ();
            driver = new ChromeDriver ();
        }
    }

    @AfterClass
    public static void stopBrowser ()
    {
        System.out.print ( "Shutting down browser..." );
        if ( driver != null )
        {
            driver.quit ();
        }
        System.out.println ( "done!" );
    }

    @BeforeClass
    public static void startClient ()
    {
        client = ClientBuilder.newBuilder ().build ();
    }

    @AfterClass
    public static void stopClient ()
    {
        if ( client != null )
        {
            client.close ();
        }
    }

    public static Client getClient ()
    {
        return client;
    }

    protected static RemoteWebDriver createSauce ( final Platform os, final String browser, final String version ) throws MalformedURLException
    {
        final DesiredCapabilities capabilities = new DesiredCapabilities ();
        capabilities.setBrowserName ( browser );
        if ( version != null )
        {
            capabilities.setVersion ( version );
        }
        capabilities.setCapability ( CapabilityType.PLATFORM, os );
        capabilities.setCapability ( CapabilityType.SUPPORTS_FINDING_BY_CSS, true );
        capabilities.setCapability ( "name", "Eclipse Package Drone Main Test" );

        if ( System.getenv ( "TRAVIS_JOB_NUMBER" ) != null )
        {
            capabilities.setCapability ( "tunnel-identifier", System.getenv ( "TRAVIS_JOB_NUMBER" ) );
            capabilities.setCapability ( "build", System.getenv ( "TRAVIS_BUILD_NUMBER" ) );
            capabilities.setCapability ( "tags", new String[] { "CI" } );
        }

        final RemoteWebDriver driver = new RemoteWebDriver ( new URL ( String.format ( "http://%s:%s@%s/wd/hub", SAUCE_USER_NAME, SAUCE_ACCESS_KEY, SAUCE_URL ) ), capabilities );

        driver.setFileDetector ( new LocalFileDetector () );

        return driver;
    }

}
