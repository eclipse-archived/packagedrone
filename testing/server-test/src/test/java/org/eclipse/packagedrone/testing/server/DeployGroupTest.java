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

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DeployGroupTest extends AbstractServerTest
{
    @Test
    public void testCreateDeployGroup () throws Exception
    {
        final WebContext ctx = getWebContext ();
        final WebDriver driver = ctx.getResolved ( "/deploy/auth/addGroup" );

        final WebElement nameField = driver.findElement ( By.id ( "name" ) );
        nameField.sendKeys ( "m1" );
        nameField.submit ();

        for ( final WebElement ele : ctx.findElements ( By.tagName ( "tr" ) ) )
        {
            final List<WebElement> cells = ele.findElements ( By.tagName ( "td" ) );
            if ( cells.isEmpty () )
            {
                // header line
                continue;
            }

            final String name = cells.get ( 0 ).getText ();
            if ( name.equals ( "m1" ) )
            {
                addKey ( cells.get ( 1 ).getText () );
            }
        }
    }

    private void addKey ( final String id )
    {
        final WebDriver driver = getWebContext ().getResolved ( "/deploy/auth/group/" + id + "/createKey" );
        final WebElement nameEle = driver.findElement ( By.id ( "name" ) );
        nameEle.sendKeys ( "key1" );
        nameEle.submit ();
    }
}
