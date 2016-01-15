/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.testing.server.channel;

import java.util.Set;

import org.eclipse.packagedrone.testing.server.AbstractServerTest;
import org.eclipse.packagedrone.testing.server.WebContext;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

public class P2Test extends AbstractServerTest
{
    @Test
    public void testGeneratedFeatureAndCategory () throws Exception
    {
        final WebContext ctx = getWebContext ();

        final ChannelTester ct = ChannelTester.create ( getWebContext (), "p2_1" );
        ct.addAspect ( "osgi" );
        ct.addAspect ( "p2.repo" );
        ct.addAspect ( "p2.metadata" );

        {
            final Set<String> result = ct.upload ( CommonResources.BUNDLE_1_RESOURCE );
            Assert.assertEquals ( 3, result.size () );
        }
        Assert.assertEquals ( 3, ct.getAllArtifactIds ().size () ); // 1 bundle + 2 p2 fragments

        // now make a generated feature

        ctx.getResolved ( String.format ( "/generators/p2.feature/channel/%s/createFeature", ct.getId () ) );
        ctx.findElement ( By.id ( "id" ) ).sendKeys ( "f1" );
        ctx.findElement ( By.id ( "version" ) ).sendKeys ( "1.0.0" );
        ctx.findElement ( By.id ( "label" ) ).sendKeys ( "Feature Test" );

        ctx.findElement ( By.id ( "command" ) ).submit ();

        Assert.assertEquals ( 7, ct.getAllArtifactIds ().size () ); // 3 from before + 1 gen feature + 1 virtual feature + 2 p2 fragments

        // now the xml generated category file

        ctx.getResolved ( String.format ( "/generators/p2.category/channel/%s/createCategoryXml", ct.getId () ) );
        ctx.findElement ( By.id ( "file" ) ).sendKeys ( ctx.getTestFile ( CommonResources.RESOURCE_BASE + "/p2/category.xml" ).toString () );

        ctx.findElement ( By.id ( "command" ) ).submit ();

        Assert.assertEquals ( 9, ct.getAllArtifactIds ().size () ); // 7 from before + 1 gen xml category + 1 p2 fragment
    }
}
