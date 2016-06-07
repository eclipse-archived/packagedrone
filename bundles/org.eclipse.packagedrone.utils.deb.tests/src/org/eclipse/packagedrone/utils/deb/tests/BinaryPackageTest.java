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
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.packagedrone.utils.deb.build.DebianPackageWriter;
import org.eclipse.packagedrone.utils.deb.build.EntryInformation;
import org.eclipse.packagedrone.utils.deb.control.BinaryPackageControlFile;
import org.junit.Assert;
import org.junit.Test;

public class BinaryPackageTest
{
    @Test
    public void test1 () throws IOException
    {
        final File file = File.createTempFile ( "test-", ".deb" );

        final BinaryPackageControlFile packageFile = new BinaryPackageControlFile ();
        packageFile.setPackage ( "test" );
        packageFile.setVersion ( "0.0.1" );
        packageFile.setArchitecture ( "all" );
        packageFile.setMaintainer ( "Jens Reimann <ctron@dentrassi.de>" );
        packageFile.setDescription ( "Test package\nThis is just a test package\n\nNothing to worry about!" );

        try ( DebianPackageWriter deb = new DebianPackageWriter ( new FileOutputStream ( file ), packageFile ) )
        {
            deb.addFile ( "Hello World\n".getBytes (), "/usr/share/foo-test/foo.txt", null );
            deb.addFile ( "Hello World\n".getBytes (), "/etc/foo.txt", EntryInformation.DEFAULT_FILE_CONF );
        }
        System.out.println ( "File: " + file );
        Assert.assertTrue ( "File exists", file.exists () );
    }
}
