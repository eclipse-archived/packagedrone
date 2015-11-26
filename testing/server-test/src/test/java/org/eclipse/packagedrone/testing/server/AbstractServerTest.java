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

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.io.CharStreams;

public class AbstractServerTest
{
    protected static RemoteWebDriver driver;

    @BeforeClass
    public static void setup ()
    {
        driver = TestSuite.getDriver ();
    }

    private final WebContext context = new WebContext () {

        @Override
        public String resolve ( final String url )
        {
            return AbstractServerTest.this.resolve ( url );
        }

        @Override
        public WebDriver getDriver ()
        {
            return driver;
        }

        @Override
        public WebElement findElement ( final By by )
        {
            return driver.findElement ( by );
        }

        @Override
        public List<WebElement> findElements ( final By by )
        {
            return driver.findElements ( by );
        }

        @Override
        public File getTestFile ( final String localFileName )
        {
            return getAbsolutePath ( localFileName );
        }
    };

    protected URL getUrl () throws MalformedURLException
    {
        return new URL ( getBase () );
    }

    protected String getBase ()
    {
        return System.getProperty ( "test.server", "http://localhost:" + TestSuite.TEST_PORT );
    }

    protected WebContext getWebContext ()
    {
        return this.context;
    }

    protected String resolve ( final String suffix, final Object... args )
    {
        try
        {
            return new URI ( getBase () ).resolve ( String.format ( suffix, args ) ).toString ();
        }
        catch ( final URISyntaxException e )
        {
            throw new RuntimeException ( e );
        }
    }

    protected File getStoreLocation ()
    {
        final String location = System.getProperty ( "test.storageLocation" );
        if ( location == null )
        {
            return new File ( "target/storage" );
        }
        return new File ( location );
    }

    protected File getAbsolutePath ( final String localPath )
    {
        final File file = new File ( localPath );
        if ( !file.exists () )
        {
            throw new IllegalStateException ( String.format ( "Unable to find file: %s", localPath ) );
        }
        return file.getAbsoluteFile ();
    }

    protected void testUrl ( final String suffix ) throws Exception
    {
        testUrl ( suffix, null );
    }

    protected void testUrl ( final String suffix, final Pattern pattern ) throws Exception
    {
        final URL url = new URL ( resolve ( suffix ) );
        try ( Reader reader = new InputStreamReader ( url.openStream () ) )
        {
            if ( pattern != null )
            {
                final String data = CharStreams.toString ( reader );
                final Matcher m = pattern.matcher ( data );

                final boolean result = m.matches ();
                if ( !result )
                {
                    System.out.println ( "Failed to match pattern: " + pattern );
                    System.out.println ( "------------------------------" );
                    System.out.println ( data );
                    System.out.println ( "------------------------------" );
                }

                Assert.assertTrue ( "Content match failed", result );
            }
        }
    }
}
