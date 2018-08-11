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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit;
import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Entry;
import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Touchpoint;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2MetaDataInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation.VersionRangedName;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Qualifiers;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement.MatchRule;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement.Type;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

public class InstallableUnitTest
{
    private static final String P2_IU_NAMESPACE = "org.eclipse.equinox.p2.iu";

    @Test
    public void test1 ()
    {
        final boolean notOptional = false;
        final Boolean nullGreedyBoolean = null;
        final String noFilter = null;
        final String bundleId = "b1";
        final String feature2Id = "f2";
        final String featureId = "f1";
        final FeatureInformation fi = new FeatureInformation ();
        fi.setId ( featureId );
        fi.setVersion ( Version.parseVersion ( "1.2.3" ) );
        fi.setQualifiers ( new Qualifiers () );
        fi.getRequirements ().add ( new Requirement ( Type.FEATURE, feature2Id, null, MatchRule.DEFAULT ) );
        fi.getRequirements ().add ( new Requirement ( Type.PLUGIN, bundleId, null, MatchRule.DEFAULT ) );

        final List<InstallableUnit> ius = InstallableUnit.fromFeature ( fi );

        assertThat ( ius.size (), is ( 2 ) );
        final InstallableUnit iuGrp = ius.get ( 0 );
        final InstallableUnit iuJar = ius.get ( 1 );
        assertThat ( iuGrp, hasRequired ( P2_IU_NAMESPACE, feature2Id + ".feature.group", "0.0.0", notOptional, nullGreedyBoolean, noFilter ) );
        assertThat ( iuGrp, hasRequired ( P2_IU_NAMESPACE, bundleId, "0.0.0", notOptional, nullGreedyBoolean, noFilter ) );
        assertThat ( iuGrp.getId (), is ( featureId + ".feature.group" ) );
        assertThat ( iuJar.getId (), is ( featureId + ".feature.jar" ) );
    }

    @Test
    public void test2 ()
    {
        final boolean notOptional = false;
        final Boolean nullGreedyBoolean = null;
        final String noFilter = null;
        final String bundleId = "b1";
        final String featureId = "f1";
        final FeatureInformation fi = new FeatureInformation ();
        fi.setId ( featureId );
        fi.setVersion ( Version.parseVersion ( "1.2.3" ) );
        fi.setQualifiers ( new Qualifiers () );
        fi.getRequirements ().add ( new Requirement ( Type.PLUGIN, bundleId, Version.parseVersion ( "1.2.0" ), MatchRule.DEFAULT ) );
        fi.getRequirements ().add ( new Requirement ( Type.PLUGIN, bundleId, Version.parseVersion ( "1.3.0" ), MatchRule.PERFECT ) );

        final List<InstallableUnit> ius = InstallableUnit.fromFeature ( fi );

        assertThat ( ius.size (), is ( 2 ) );
        final InstallableUnit iuGrp = ius.get ( 0 );
        final InstallableUnit iuJar = ius.get ( 1 );
        assertThat ( iuGrp, hasRequired ( P2_IU_NAMESPACE, featureId + ".feature.jar", "[1.2.3,1.2.3]", notOptional, nullGreedyBoolean, "(org.eclipse.update.install.features=true)" ) );
        assertThat ( iuGrp, hasRequired ( P2_IU_NAMESPACE, bundleId, "0.0.0", notOptional, nullGreedyBoolean, noFilter ) );
        assertThat ( iuGrp, hasRequired ( P2_IU_NAMESPACE, bundleId, "[1.3.0,1.3.0]", notOptional, nullGreedyBoolean, noFilter ) );
        assertThat ( iuGrp.getId (), is ( featureId + ".feature.group" ) );
        assertThat ( iuJar.getId (), is ( featureId + ".feature.jar" ) );
    }

    @Test
    public void addsZippedInstructionOnDirShape () throws Exception
    {
        final P2MetaDataInformation p2info = new P2MetaDataInformation ();
        final BundleInformation bi = new BundleInformation ();
        bi.setEclipseBundleShape ( "dir" );

        final InstallableUnit iu = InstallableUnit.fromBundle ( bi, p2info );

        final Touchpoint touchpoint = iu.getTouchpoints ().get ( 0 );
        assertThat ( touchpoint.getInstructions (), hasEntry ( "zipped", is ( "true" ) ) );
    }

    @Test
    public void fragmentSpecificPropertiesAddedToIU () throws Exception
    {
        final boolean notOptional = false;
        final Boolean nullGreedyBoolean = null;
        final String noFilter = null;
        final String hostBundleName = "org.host.bundle";
        final String ownVersion = "1.0.7";
        final String hostVersionRange = "[1.0.1,2.0.0)";
        final VersionRangedName hostBundle = new VersionRangedName ( hostBundleName, VersionRange.valueOf ( hostVersionRange ) );
        final BundleInformation bi = new BundleInformation ();
        bi.setFragmentHost ( hostBundle );
        bi.setVersion ( Version.parseVersion ( ownVersion ) );
        final P2MetaDataInformation p2info = new P2MetaDataInformation ();

        final InstallableUnit iu = InstallableUnit.fromBundle ( bi, p2info );

        assertThat ( iu, hasProvided ( "osgi.fragment", hostBundleName, ownVersion ) );
        assertThat ( iu, hasRequired ( "osgi.bundle", hostBundleName, hostVersionRange, notOptional, nullGreedyBoolean, noFilter ) );
        final String fragmentHostEntry = Constants.FRAGMENT_HOST + ": " + hostBundleName + ";" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + hostVersionRange + "\"";
        final Map<String, String> touchpointInstructions = iu.getTouchpoints ().get ( 0 ).getInstructions ();
        assertThat ( touchpointInstructions, hasEntry ( "manifest", containsString ( fragmentHostEntry ) ) );
    }

    static private final Matcher<InstallableUnit> hasProvided ( final String namespace, final String name, final String version )
    {
        return new CustomTypeSafeMatcher<InstallableUnit> ( "IU with 'provided'-element [namespace=" + namespace + ", name=" + name + ", version=" + version + " ]" ) {

            @Override
            protected boolean matchesSafely ( final InstallableUnit item )
            {
                final List<Entry<String>> provides = item.getProvides ();
                for ( final Entry<String> provide : provides )
                {
                    if ( Objects.equals ( provide.getNamespace (), namespace ) && Objects.equals ( provide.getKey (), name ) && Objects.equals ( provide.getValue (), version ) )
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    static private final Matcher<InstallableUnit> hasRequired ( final String namespace, final String name, final String versionRange, final boolean isOptional, final Boolean greedy, final String filter )
    {
        return new CustomTypeSafeMatcher<InstallableUnit> ( "IU with 'required'-element [namespace= " + namespace + ", name= " + name + ", versionRange= " + versionRange + " ]" ) {

            @Override
            protected boolean matchesSafely ( final InstallableUnit item )
            {
                final List<Entry<org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Requirement>> requires = item.getRequires ();
                for ( final Entry<org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Requirement> require : requires )
                {

                    if ( Objects.equals ( require.getNamespace (), namespace ) && Objects.equals ( require.getKey (), name ) )
                    {
                        final org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit.Requirement value = require.getValue ();
                        if ( Objects.equals ( value.getRange (), VersionRange.valueOf ( versionRange ) ) && Objects.equals ( value.getFilter (), filter ) && Objects.equals ( value.getGreedy (), greedy ) )
                        {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    static private final <K, V> Matcher<Map<K, V>> hasEntry ( final K key, final Matcher<V> valueMatcher )
    {
        return new CustomTypeSafeMatcher<Map<K, V>> ( "Map with entry: key=" + key + ", value=" + valueMatcher ) {

            @Override
            protected boolean matchesSafely ( final Map<K, V> item )
            {
                final Object actualValue = item.get ( key );
                if ( valueMatcher.matches ( actualValue ) )
                {
                    return true;
                }
                return false;
            }
        };
    }
}
