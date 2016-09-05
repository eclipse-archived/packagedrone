/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.tests;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bouncycastle.openpgp.PGPException;
import org.eclipse.packagedrone.utils.rpm.app.Dumper;
import org.eclipse.packagedrone.utils.rpm.build.RpmBuilder;
import org.eclipse.packagedrone.utils.rpm.build.RpmBuilder.PackageInformation;
import org.eclipse.packagedrone.utils.rpm.parse.RpmInputStream;
import org.junit.BeforeClass;
import org.junit.Test;

public class EmptyWriterTest
{
    private static final Path OUT_BASE = Paths.get ( "data", "out.empty" );

    @BeforeClass
    public static void setup () throws IOException
    {
        Files.createDirectories ( OUT_BASE );
    }

    @Test
    public void test3 () throws IOException, PGPException
    {
        Path outFile;

        try ( RpmBuilder builder = new RpmBuilder ( "testEmpty", "1.0.0", "1", "noarch", OUT_BASE ) )
        {
            final PackageInformation pinfo = builder.getInformation ();

            pinfo.setLicense ( "EPL" );
            pinfo.setSummary ( "Foo bar" );
            pinfo.setVendor ( "Eclipse Package Drone Project" );
            pinfo.setDescription ( "This is an empty test package" );
            pinfo.setDistribution ( "Eclipse Package Drone" );

            builder.setPreInstallationScript ( "true # test call" );

            outFile = builder.getTargetFile ();

            builder.build ();
        }

        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( Files.newInputStream ( outFile ) ) ) )
        {
            Dumper.dumpAll ( in );
        }

        System.out.println ( outFile.toAbsolutePath () );
    }
}
