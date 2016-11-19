/*******************************************************************************
 * Copyright (c) 2016 Bachmann electronic GmbH. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bachmann electronic GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.rpm.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.Severity;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RpmExtractorTest
{

    final RpmExtractor extractor = new RpmExtractor ();

    static Path rpmFile;

    @BeforeClass
    public static void setup () throws IOException
    {
        rpmFile = Files.createTempFile ( "RpmExtractorTestData", String.valueOf ( System.currentTimeMillis () ) );
        Files.copy ( RpmExtractorTest.class.getResourceAsStream ( "/data/org.eclipse.scada-0.2.1-1.noarch.rpm" ), rpmFile, StandardCopyOption.REPLACE_EXISTING );
    }

    @AfterClass
    public static void tearDown () throws IOException
    {
        Files.deleteIfExists ( rpmFile );

    }

    @Test
    public void testMetadataExtraction ()
    {
        final Extractor.Context context = new TestContext ();
        final Map<String, String> metadata = new HashMap<> ();
        this.extractor.extractMetaData ( context, metadata );
        Assert.assertEquals ( "RPM Package", metadata.get ( "artifactLabel" ) );
        Assert.assertEquals ( "org.eclipse.scada", metadata.get ( "name" ) );
        Assert.assertEquals ( "0.2.1", metadata.get ( "version" ) );
        Assert.assertEquals ( "linux", metadata.get ( "os" ) );
        Assert.assertEquals ( "noarch", metadata.get ( "arch" ) );
        Assert.assertEquals ( "1", metadata.get ( "release" ) );
    }

    class TestContext implements Extractor.Context
    {
        @Override
        public void validationMessage ( final Severity severity, final String message )
        {
            // noop
        }

        @Override
        public Path getPath ()
        {
            return RpmExtractorTest.rpmFile;
        }

        @Override
        public String getName ()
        {
            return null;
        }

        @Override
        public Instant getCreationTimestamp ()
        {
            return null;
        }
    }
}
