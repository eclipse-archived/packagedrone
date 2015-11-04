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
package org.eclipse.packagedrone.utils.rpm.tests;

import java.util.Optional;

import org.eclipse.packagedrone.utils.rpm.RpmVersion;
import org.junit.Assert;
import org.junit.Test;

public class VersionTest
{
    @Test
    public void test1 ()
    {
        testVersion ( "1.2.3", null, "1.2.3", null );
    }

    @Test
    public void test2 ()
    {
        testVersion ( "0:1.2.3", 0, "1.2.3", null );
    }

    @Test
    public void test3 ()
    {
        testVersion ( "0:1.2.3-1", 0, "1.2.3", "1" );
    }

    @Test
    public void test4 ()
    {
        testVersion ( "1.2.3-1", null, "1.2.3", "1" );
    }

    @Test
    public void test5 ()
    {
        testVersion ( "1.2.3-123-456", null, "1.2.3", "123-456" );
    }

    private void testVersion ( final String version, final Integer expectedEpoch, final String expectedVersion, final String expectedRelease )
    {
        final RpmVersion v = RpmVersion.valueOf ( version );
        Assert.assertEquals ( "Epoch", Optional.ofNullable ( expectedEpoch ), v.getEpoch () );
        Assert.assertEquals ( "Version", expectedVersion, v.getVersion () );
        Assert.assertEquals ( "Release", Optional.ofNullable ( expectedRelease ), v.getRelease () );
    }
}
