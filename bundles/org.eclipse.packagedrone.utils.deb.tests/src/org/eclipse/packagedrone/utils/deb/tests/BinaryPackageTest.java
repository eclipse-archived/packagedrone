/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.deb.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.packagedrone.utils.deb.build.DebianPackageWriter;
import org.eclipse.packagedrone.utils.deb.build.EntryInformation;
import org.eclipse.packagedrone.utils.deb.control.BinaryPackageControlFile;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.hash.Hashing;

public class BinaryPackageTest
{
    @Test
    public void test1 () throws IOException, InterruptedException
    {
        final File file1 = File.createTempFile ( "test-1-", ".deb" );
        final File file2 = File.createTempFile ( "test-2-", ".deb" );

        final Instant now = Instant.now ();
        final Supplier<Instant> timestampProvider = () -> now;

        createDebFile ( file1, timestampProvider );
        System.out.println ( "File: " + file1 );
        Assert.assertTrue ( "File exists", file1.exists () );

        Thread.sleep ( 1_001 ); // sleep for a second to make sure that a timestamp might be changed

        createDebFile ( file2, timestampProvider );
        System.out.println ( "File: " + file2 );
        Assert.assertTrue ( "File exists", file2.exists () );

        final byte[] b1 = Files.readAllBytes ( file1.toPath () );
        final String h1 = Hashing.md5 ().hashBytes ( b1 ).toString ();
        final byte[] b2 = Files.readAllBytes ( file2.toPath () );
        final String h2 = Hashing.md5 ().hashBytes ( b2 ).toString ();
        System.out.println ( h1 );
        System.out.println ( h2 );
        Assert.assertEquals ( h1, h2 );
    }

    private void createDebFile ( final File file, final Supplier<Instant> timestampProvider ) throws IOException, FileNotFoundException
    {
        final BinaryPackageControlFile packageFile = new BinaryPackageControlFile ();
        packageFile.setPackage ( "test" );
        packageFile.setVersion ( "0.0.1" );
        packageFile.setArchitecture ( "all" );
        packageFile.setMaintainer ( "Jens Reimann <ctron@dentrassi.de>" );
        packageFile.setDescription ( "Test package\nThis is just a test package\n\nNothing to worry about!" );

        try ( DebianPackageWriter deb = new DebianPackageWriter ( new FileOutputStream ( file ), packageFile ) )
        {
            deb.addFile ( "Hello World\n".getBytes (), "/usr/share/foo-test/foo.txt", null, Optional.of ( timestampProvider ) );
            deb.addFile ( "Hello World\n".getBytes (), "/etc/foo.txt", EntryInformation.DEFAULT_FILE_CONF, Optional.of ( timestampProvider ) );
        }
    }
}
