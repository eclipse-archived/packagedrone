/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit;
import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Entry;
import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Touchpoint;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2MetaDataInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Qualifiers;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement.MatchRule;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement.Type;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
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

        assertFeatureGroup1 ( ius.get ( 0 ) );
        assertFeatureJar1 ( ius.get ( 1 ) );
    }

    private void assertFeatureJar1 ( final InstallableUnit iu )
    {
        assertNotNull ( iu );
        assertEquals ( "f1.feature.jar", iu.getId () );
    }

    private void assertFeatureGroup1 ( final InstallableUnit iu )
    {
        assertNotNull ( iu );
        assertEquals ( "f1.feature.group", iu.getId () );

        assertHasRequirementPresent ( iu, "org.eclipse.equinox.p2.iu", "f2.feature.group", 1 );
        assertHasRequirementPresent ( iu, "org.eclipse.equinox.p2.iu", "b1", 1 );
    }

    @Test
    public void test2 ()
    {
        final FeatureInformation fi = new FeatureInformation ();

        fi.setId ( "f1" );
        fi.setVersion ( Version.parseVersion ( "1.2.3" ) );
        fi.setQualifiers ( new Qualifiers () );

        fi.getRequirements ().add ( new Requirement ( Type.PLUGIN, "b1", Version.parseVersion ( "1.2.0" ), MatchRule.DEFAULT ) );
        fi.getRequirements ().add ( new Requirement ( Type.PLUGIN, "b1", Version.parseVersion ( "1.3.0" ), MatchRule.DEFAULT ) );

        final List<InstallableUnit> ius = InstallableUnit.fromFeature ( fi );

        assertEquals ( 2, ius.size () );

        assertFeatureGroup2 ( ius.get ( 0 ) );
        assertFeatureJar2 ( ius.get ( 1 ) );
    }

    private void assertFeatureJar2 ( final InstallableUnit iu )
    {
        assertNotNull ( iu );
        assertEquals ( "f1.feature.jar", iu.getId () );
    }

    private void assertFeatureGroup2 ( final InstallableUnit iu )
    {
        assertNotNull ( iu );
        assertEquals ( "f1.feature.group", iu.getId () );

        assertHasRequirementPresent ( iu, "org.eclipse.equinox.p2.iu", "b1", 2 );
    }

    private static void assertHasRequirementPresent ( final InstallableUnit iu, final String namespace, final String key, final int amount )
    {
        final List<Entry<org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Requirement>> req = findRequirement ( iu, namespace, key );

        assertEquals ( amount, req.size () );
    }

    private static List<Entry<org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Requirement>> findRequirement ( final InstallableUnit iu, final String namespace, final String key )
    {
        return iu.getRequires ().stream ().filter ( entry -> entry.getNamespace ().equals ( namespace ) && entry.getKey ().equals ( key ) ).collect ( Collectors.toList () );
    }

    @Test
    public void addsZippedInstructionOnDirShape () throws Exception
    {
        final P2MetaDataInformation p2info = new P2MetaDataInformation ();
        final BundleInformation bi = new BundleInformation ();
        bi.setEclipseBundleShape ( "dir" );

        final InstallableUnit iu = InstallableUnit.fromBundle ( bi, p2info );

        final Touchpoint touchpoint = iu.getTouchpoints ().get ( 0 );
        assertThat ( touchpoint.getInstructions (), hasEntry ( "zipped", "true" ) );
    }

    static private final Matcher<Map<?, ?>> hasEntry ( final Object key, final Object value )
    {
        return new CustomTypeSafeMatcher<Map<?, ?>> ( "" ) {

            @Override
            protected boolean matchesSafely ( final Map<?, ?> item )
            {
                if ( Objects.equals ( item.get ( key ), value ) )
                {
                    return true;
                }
                return false;
            }
        };
    }
}
