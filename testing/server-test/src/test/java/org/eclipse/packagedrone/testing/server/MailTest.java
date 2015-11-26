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

public class MailTest extends AbstractServerTest
{

    @Test
    public void testConfig ()
    {
        driver.get ( resolve ( "/default.mail/config" ) );

        // check if we are on the right page

        Assert.assertEquals ( resolve ( "/default.mail/config" ), driver.getCurrentUrl () );

        // must be false now

        System.out.println ( "Check first" );

        Assert.assertEquals ( driver.findElementById ( "servicePresent" ).getText (), "false" );

        driver.findElementById ( "host" ).sendKeys ( "localhost" );
        driver.findElementById ( "command" ).submit ();

        /*
        final WebDriverWait wait = new WebDriverWait ( driver, 5 );
        wait.until ( new Predicate<WebDriver> () {
        
            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.findElementById ( "servicePresent" ).getText ().equals ( "true" );
            }
        } );
        */

        /* do a reload ..
         * this should not be necessary but it seems that setting and reloading
         * occurs to fast so that the service is shortly gone during re-config state.
         *
         * So we reload and check this result
         */

        driver.get ( resolve ( "/default.mail/config" ) );

        System.out.println ( "Check second" );

        // must be true now
        Assert.assertEquals ( driver.findElementById ( "servicePresent" ).getText (), "true" );
    }
}
