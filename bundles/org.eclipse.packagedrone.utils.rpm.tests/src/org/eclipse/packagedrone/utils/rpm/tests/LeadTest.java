/*******************************************************************************
 * Copyright (c) 2016, 2018 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - test lead mappers
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.tests;

import static java.util.Optional.of;

import java.util.Optional;

import org.eclipse.packagedrone.utils.rpm.Architecture;
import org.eclipse.packagedrone.utils.rpm.OperatingSystem;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.build.LeadBuilder;
import org.eclipse.packagedrone.utils.rpm.header.Header;
import org.junit.Assert;
import org.junit.Test;

public class LeadTest
{
    @Test
    public void testArch1 ()
    {
        testArch ( Architecture.INTEL, "i386" );
        testArch ( Architecture.INTEL, "INTEL" );
        testArch ( Architecture.INTEL, "X86_64" );
    }

    private void testArch ( final Architecture expected, final String provided )
    {
        final Optional<Architecture> arch = Architecture.fromAlias ( provided );
        if ( expected == null )
        {
            Assert.assertFalse ( arch.isPresent () );
        }
        else
        {
            Assert.assertEquals ( expected, arch.orElse ( null ) );
        }
    }

    /**
     * Test the mappers for arch and os.
     */
    @Test
    public void testMapper1 ()
    {
        final LeadBuilder lead = new LeadBuilder ();
        final Header<RpmTag> header = new Header<> ();

        header.putString ( RpmTag.ARCH, "foo-bar" );
        header.putString ( RpmTag.OS, "bar-foo" );

        lead.fillFlagsFromHeader ( header );

        Assert.assertEquals ( Architecture.NOARCH.getValue (), lead.getArchitecture () );
        Assert.assertEquals ( OperatingSystem.UNKNOWN.getValue (), lead.getOperatingSystem () );

        lead.fillFlagsFromHeader ( header, s -> of ( Architecture.ARM ), s -> of ( OperatingSystem.AIX ) );

        Assert.assertEquals ( Architecture.ARM.getValue (), lead.getArchitecture () );
        Assert.assertEquals ( OperatingSystem.AIX.getValue (), lead.getOperatingSystem () );
    }
}
