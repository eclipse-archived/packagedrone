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

public class RpmTest extends AbstractServerTest
{
    private static final String RPM_RESOURCE_1 = "src/test/resources/rpm/org.eclipse.scada-0.2.1-1.noarch.rpm";

    private static final String RPM_RESOURCE_2 = "src/test/resources/rpm/org.eclipse.scada-centos6-0.2.1-1.noarch.rpm";

    @Test
    public void testRpm1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "rpm1" );
        ct.addAspect ( "rpm" );
        ct.addAspect ( "yum" );

        {
            final Set<String> result = ct.upload ( RPM_RESOURCE_1 );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 1, ct.getAllArtifactIds ().size () );

        // upload a second artifact

        {
            final Set<String> result = ct.upload ( RPM_RESOURCE_2 );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 2, ct.getAllArtifactIds ().size () );

        testUrl ( String.format ( "/yum/%s/", ct.getId () ), Pattern.compile ( ".*YUM repository.*", Pattern.DOTALL ) );
        testUrl ( String.format ( "/yum/%s/repodata/", ct.getId () ) );
        testUrl ( String.format ( "/yum/%s/repodata/repomd.xml", ct.getId () ) );
    }

}
