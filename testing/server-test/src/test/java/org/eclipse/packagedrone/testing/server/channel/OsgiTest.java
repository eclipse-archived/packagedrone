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

import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.packagedrone.testing.server.AbstractServerTest;
import org.junit.Assert;
import org.junit.Test;

public class OsgiTest extends AbstractServerTest
{
    @Test
    public void testPlainOsgi1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "osgi1" );
        ct.addAspect ( "osgi" );

        {
            final Set<String> result = ct.upload ( CommonResources.BUNDLE_1_RESOURCE );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 1, ct.getAllArtifactIds ().size () );

        // upload the same artifact a second time

        {
            final Set<String> result = ct.upload ( CommonResources.BUNDLE_1_RESOURCE );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 2, ct.getAllArtifactIds ().size () );
    }

    @Test
    public void testP2Osgi1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "osgi2" );
        ct.addAspect ( "osgi" );
        ct.addAspect ( "p2.repo" );
        ct.addAspect ( "p2.metadata" );

        {
            final Set<String> result = ct.upload ( CommonResources.BUNDLE_1_RESOURCE );
            Assert.assertEquals ( 3, result.size () ); // expect 1 bundle + 2 p2 fragments
        }
        Assert.assertEquals ( 3, ct.getAllArtifactIds ().size () );

        // upload the same artifact a second time

        {
            final Set<String> result = ct.upload ( CommonResources.BUNDLE_1_RESOURCE );
            Assert.assertEquals ( 3, result.size () ); // expect 1 bundle + 2 p2 fragments
        }
        Assert.assertEquals ( 6, ct.getAllArtifactIds ().size () );

        testUrl ( "/p2/" + ct.getId (), Pattern.compile ( ".*Package Drone Channel\\:.*", Pattern.DOTALL ) );
    }

    @Test
    public void testR5Osgi1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "osgi3" );
        ct.addAspect ( "osgi" );
        ct.addAspect ( "r5.repo" );

        {
            final Set<String> result = ct.upload ( CommonResources.BUNDLE_1_RESOURCE );
            Assert.assertEquals ( 1, result.size () ); // expect 1 bundle
        }
        Assert.assertEquals ( 1, ct.getAllArtifactIds ().size () );

        testUrl ( "/r5/" + ct.getId () );
    }

}
