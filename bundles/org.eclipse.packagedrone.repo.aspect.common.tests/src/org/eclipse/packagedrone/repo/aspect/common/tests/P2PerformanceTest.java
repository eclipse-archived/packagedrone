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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import javax.xml.stream.XMLOutputFactory;

import org.eclipse.packagedrone.repo.aspect.common.p2.internal.Creator;
import org.eclipse.packagedrone.repo.aspect.common.p2.internal.Creator.Context;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.utils.io.IOConsumer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Version;

public class P2PerformanceTest
{
    private static Context context;

    private static ThreadLocal<XMLOutputFactory> factoryLocal = ThreadLocal.withInitial ( XMLOutputFactory::newFactory );

    @BeforeClass
    public static void setup ()
    {
        context = new Creator.Context () {

            @Override
            public void create ( final String name, final IOConsumer<OutputStream> producer ) throws IOException
            {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
                producer.accept ( bos );
            }
        };
    }

    @Test
    public void test1 () throws Exception
    {
        final Creator creator = new Creator ( P2PerformanceTest.context, factoryLocal::get );

        final ArtifactInformation artifact = new ArtifactInformation ( "abc", null, null, "foo-bar.jar", 100_000, Instant.now (), Collections.singleton ( "stored" ), Collections.emptyList (), null, null, null );
        final BundleInformation bi = new BundleInformation ();
        bi.setId ( "foo-bar" );
        bi.setVersion ( new Version ( "1.0.0" ) );
        bi.setName ( "Foo Bar" );
        bi.setVendor ( "Some Vendor" );

        final Instant start = Instant.now ();

        final int it = 50_000;

        for ( int i = 0; i < it; i++ )
        {
            creator.createBundleP2Artifacts ( artifact, bi );
        }

        final Duration diff = Duration.between ( start, Instant.now () );
        System.out.format ( "%s ms (%.2f ms/it)", diff.toMillis (), (double)diff.toMillis () / (double)it );
    }
}
