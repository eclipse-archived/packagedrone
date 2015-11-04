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
package org.eclipse.packagedrone.repo.aspect.common.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit;
import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Key;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Qualifiers;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement.MatchRule;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement.Type;
import org.junit.Test;
import org.osgi.framework.Version;

public class InstallableUnitTest
{
    @Test
    public void test1 ()
    {
        final FeatureInformation fi = new FeatureInformation ();

        fi.setId ( "f1" );
        fi.setVersion ( Version.parseVersion ( "1.2.3" ) );
        fi.setQualifiers ( new Qualifiers () );

        fi.getRequirements ().add ( new Requirement ( Type.FEATURE, "f2", null, MatchRule.DEFAULT ) );
        fi.getRequirements ().add ( new Requirement ( Type.PLUGIN, "b1", null, MatchRule.DEFAULT ) );

        final List<InstallableUnit> ius = InstallableUnit.fromFeature ( fi );

        assertEquals ( 2, ius.size () );

        assertFeatureGroup ( ius.get ( 0 ) );
        assertFeatureJar ( ius.get ( 1 ) );
    }

    private void assertFeatureJar ( final InstallableUnit iu )
    {
        assertNotNull ( iu );
        assertEquals ( "f1.feature.jar", iu.getId () );
    }

    private void assertFeatureGroup ( final InstallableUnit iu )
    {
        assertNotNull ( iu );
        assertEquals ( "f1.feature.group", iu.getId () );

        final org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Requirement req1 = iu.getRequires ().get ( new Key ( "org.eclipse.equinox.p2.iu", "f2.feature.group" ) );

        assertNotNull ( req1 );

        final org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Requirement req2 = iu.getRequires ().get ( new Key ( "org.eclipse.equinox.p2.iu", "b1" ) );

        assertNotNull ( req2 );
    }
}
