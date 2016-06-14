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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

public class UserTest extends AbstractServerTest
{

    private static final String TEST_USER_REAL_NAME = "Drone Tester";

    private static final String TEST_USER_EMAIL = "dronetester@dentrassi.de";

    private static final String TEST_USER_PASSWORD = "123456";

    @Test
    public void testLogin () throws IOException
    {
        System.out.println ( "testLogin ()" );
        // Sign in first

        final String[] adminToken = Server.loadAdminToken ();

        driver.get ( resolve ( "/login" ) );
        Assert.assertEquals ( resolve ( "/login" ), driver.getCurrentUrl () );

        driver.findElementById ( "email" ).sendKeys ( adminToken[0] );
        driver.findElementById ( "password" ).sendKeys ( adminToken[1] );

        driver.findElementById ( "command" ).submit ();
    }

    private static Predicate<WebDriver> endsWith ( final String urlSuffix )
    {
        return new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver driver )
            {
                return driver.getCurrentUrl ().endsWith ( urlSuffix );
            }
        };
    }

    @Test
    public void testConfig () throws IOException
    {
        System.out.println ( "testConfig ()" );

        driver.get ( resolve ( "/user" ) );

        // check if we are on the right page

        Assert.assertEquals ( resolve ( "/user" ), driver.getCurrentUrl () );

        // add

        driver.get ( resolve ( "/user/add" ) );

        Assert.assertEquals ( resolve ( "/user/add" ), driver.getCurrentUrl () );

        // enter

        driver.findElementById ( "email" ).sendKeys ( TEST_USER_EMAIL );
        driver.findElementById ( "name" ).sendKeys ( TEST_USER_REAL_NAME );
        driver.findElementById ( "command" ).submit ();

        // Assert.assertTrue ( driver.getCurrentUrl ().endsWith ( "/view" ) );
        wait.until ( endsWith ( "/view" ) );

        final String userId = driver.findElementById ( "userId" ).getText ();

        System.out.println ( "Button: " + driver.findElementByClassName ( "btn-primary" ).getText () );
        wait.until ( ExpectedConditions.elementToBeClickable ( By.className ( "btn-primary" ) ) ).click ();

        wait.until ( endsWith ( "/" + userId + "/edit" ) );

        final WebElement newRoleInput = driver.findElement ( By.id ( "newRole" ) );
        final WebElement newRoleButton = driver.findElement ( By.id ( "btnNewRole" ) );

        newRole ( newRoleInput, newRoleButton, "ADMIN" );
        newRole ( newRoleInput, newRoleButton, "MANAGER" );

        driver.findElementById ( "command" ).submit ();

        new WebDriverWait ( driver, 5 ).until ( endsWith ( "/" + userId + "/view" ) );

        System.out.println ( "User created" );

        driver.get ( resolve ( "/user/%s/newPassword", userId ) );
        driver.findElement ( By.id ( "password" ) ).sendKeys ( TEST_USER_PASSWORD );
        driver.findElement ( By.id ( "passwordRepeat" ) ).sendKeys ( TEST_USER_PASSWORD );
        driver.findElement ( By.id ( "password" ) ).submit ();

        System.out.println ( "Password set" );

        driver.get ( resolve ( "/logout" ) );
        driver.get ( resolve ( "/login" ) );

        driver.findElementById ( "email" ).sendKeys ( TEST_USER_EMAIL );
        driver.findElementById ( "password" ).sendKeys ( TEST_USER_PASSWORD );
        driver.findElementById ( "command" ).submit ();

        System.out.println ( "User logged in" );
    }

    private void newRole ( final WebElement newRoleInput, final WebElement newRoleButton, final String role )
    {
        newRoleInput.clear ();
        newRoleInput.sendKeys ( role );
        newRoleButton.click ();
    }
}
