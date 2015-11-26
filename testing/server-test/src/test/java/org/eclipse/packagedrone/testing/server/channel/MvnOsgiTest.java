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

import org.eclipse.packagedrone.testing.server.AbstractServerTest;
import org.junit.Assert;
import org.junit.Test;

public class MvnOsgiTest extends AbstractServerTest
{
    @Test
    public void testMvnOsgi1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "mvnosgi1" );
        ct.addAspect ( "mvnosgi" );

        {
            final Set<String> result = ct.upload ( CommonResources.BUNDLE_1_RESOURCE );
            Assert.assertEquals ( 2, result.size () );
        }
        Assert.assertEquals ( 2, ct.getAllArtifactIds ().size () );

        testUrl ( String.format ( "/maven/%s/mvnosgi/org.eclipse.scada.utils/maven-metadata.xml", ct.getId () ) );
    }
}
