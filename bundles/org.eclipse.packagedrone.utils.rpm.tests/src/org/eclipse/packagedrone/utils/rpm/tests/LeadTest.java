/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
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

import org.eclipse.packagedrone.utils.rpm.Architecture;
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
}
