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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.packagedrone.utils.rpm.RpmInputStream;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.app.Dumper;
import org.eclipse.packagedrone.utils.rpm.build.BuilderContext;
import org.eclipse.packagedrone.utils.rpm.build.PayloadRecorder;
import org.eclipse.packagedrone.utils.rpm.build.RpmBuilder;
import org.eclipse.packagedrone.utils.rpm.build.RpmBuilder.PackageInformation;
import org.eclipse.packagedrone.utils.rpm.build.RpmWriter;
import org.eclipse.packagedrone.utils.rpm.deps.Dependencies;
import org.eclipse.packagedrone.utils.rpm.deps.Dependency;
import org.eclipse.packagedrone.utils.rpm.deps.RpmDependencyFlags;
import org.eclipse.packagedrone.utils.rpm.header.Header;
import org.junit.BeforeClass;
import org.junit.Test;

public class WriterTest
{
    private static final Path OUT_BASE = Paths.get ( "data", "out" );

    private static final Path IN_BASE = Paths.get ( "data", "in" );

    @BeforeClass
    public static void setup () throws IOException
    {
        Files.createDirectories ( OUT_BASE );
    }

    @Test
    public void test1 () throws IOException
    {
        final Path rpm1 = OUT_BASE.resolve ( "test1-1.0.0.rpm" );

        final Header<RpmTag> header = new Header<> ();

        header.putString ( RpmTag.PAYLOAD_FORMAT, "cpio" );
        header.putString ( RpmTag.PAYLOAD_CODING, "gzip" );
        header.putString ( RpmTag.PAYLOAD_FLAGS, "9" );
        header.putStringArray ( 100, "C" );

        header.putString ( RpmTag.NAME, "test1" );
        header.putString ( RpmTag.VERSION, "1.0.0" );
        header.putString ( RpmTag.RELEASE, "1" );
        header.putI18nString ( RpmTag.SUMMARY, "foo bar" );
        header.putI18nString ( RpmTag.DESCRIPTION, "foo bar2" );
        header.putString ( RpmTag.LICENSE, "EPL" );
        header.putString ( RpmTag.GROUP, "Unspecified" );
        header.putString ( RpmTag.ARCH, "noarch" );
        header.putString ( RpmTag.OS, "linux" );
        header.putInt ( RpmTag.BUILDTIME, 1459865130 );
        header.putString ( RpmTag.BUILDHOST, "localhost" );
        header.putInt ( RpmTag.SIZE, 0 );

        final List<Dependency> provides = new LinkedList<> ();
        provides.add ( new Dependency ( "test1", "1.0.0", RpmDependencyFlags.EQUAL ) );
        Dependencies.putProvides ( header, provides );

        final List<Dependency> requirements = new LinkedList<> ();
        requirements.add ( new Dependency ( "rpmlib(PayloadFilesHavePrefix)", "4.0-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
        requirements.add ( new Dependency ( "rpmlib(CompressedFileNames)", "3.0.4-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
        Dependencies.putRequirements ( header, requirements );

        try ( PayloadRecorder recorder = new PayloadRecorder () )
        {
            try ( RpmWriter writer = new RpmWriter ( rpm1, "test1-1.0.0", header ) )
            {
                writer.setPayload ( recorder );
            }
        }

        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( Files.newInputStream ( rpm1 ) ) ) )
        {
            Dumper.dumpAll ( in );
        }
    }

    @Test
    public void test2 () throws IOException
    {
        final Path outFile = OUT_BASE.resolve ( "test2-1.0.0.1.rpm" );

        try ( PayloadRecorder payload = new PayloadRecorder () )
        {
            final Header<RpmTag> header = new Header<> ();

            header.putString ( RpmTag.PAYLOAD_FORMAT, "cpio" );
            header.putString ( RpmTag.PAYLOAD_CODING, "gzip" );
            header.putString ( RpmTag.PAYLOAD_FLAGS, "9" );
            header.putStringArray ( 100, "C" );

            header.putString ( RpmTag.NAME, "test2" );
            header.putString ( RpmTag.VERSION, "1.0.0" );
            header.putString ( RpmTag.RELEASE, "1" );

            header.putI18nString ( RpmTag.SUMMARY, "foo bar" );
            header.putI18nString ( RpmTag.DESCRIPTION, "foo bar2" );

            header.putString ( RpmTag.LICENSE, "EPL" );
            header.putString ( RpmTag.GROUP, "Unspecified" );
            header.putString ( RpmTag.ARCH, "noarch" );
            header.putString ( RpmTag.OS, "linux" );
            header.putInt ( RpmTag.BUILDTIME, (int) ( System.currentTimeMillis () / 1000 ) );
            header.putString ( RpmTag.BUILDHOST, "localhost" );

            final List<Dependency> provides = new LinkedList<> ();
            provides.add ( new Dependency ( "test2", "1.0.0", RpmDependencyFlags.EQUAL ) );
            Dependencies.putProvides ( header, provides );

            final List<Dependency> requirements = new LinkedList<> ();
            requirements.add ( new Dependency ( "rpmlib(PayloadFilesHavePrefix)", "4.0-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
            requirements.add ( new Dependency ( "rpmlib(CompressedFileNames)", "3.0.4-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
            Dependencies.putRequirements ( header, requirements );

            int installedSize = 0;
            installedSize += payload.addFile ( "/etc/test3/file1", IN_BASE.resolve ( "file1" ) ).getSize ();

            header.putInt ( RpmTag.SIZE, installedSize );

            try ( RpmWriter writer = new RpmWriter ( outFile, "test2", header ) )
            {
                writer.setPayload ( payload );
            }
        }

        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( Files.newInputStream ( outFile ) ) ) )
        {
            Dumper.dumpAll ( in );
        }
    }

    @Test
    public void test3 () throws IOException
    {
        Path outFile;

        try ( RpmBuilder builder = new RpmBuilder ( "test3", "1.0.0", "1", "noarch", OUT_BASE ) )
        {
            final PackageInformation pinfo = builder.getInformation ();

            pinfo.setLicense ( "EPL" );
            pinfo.setSummary ( "Foo bar" );
            pinfo.setVendor ( "Eclipse Package Drone Project" );
            pinfo.setDescription ( "This is a test package" );
            pinfo.setDistribution ( "Eclipse Package Drone" );

            final BuilderContext ctx = builder.newContext ();
            // ctx.setDefaultInformationProvider ( BuilderContext.defaultProvider () );

            ctx.addDirectory ( "/etc/test3" );

            ctx.addDirectory ( "/var/lib/test3", finfo -> {
            } );

            ctx.addFile ( "/etc/test3/file1", IN_BASE.resolve ( "file1" ), BuilderContext.pathProvider ().customize ( finfo -> {
                finfo.setConfiguration ( true );
            } ) );

            ctx.addFile ( "/etc/test3/file2", new ByteArrayInputStream ( "foo".getBytes ( StandardCharsets.UTF_8 ) ), finfo -> {
                finfo.setTimestamp ( LocalDateTime.of ( 2014, 1, 1, 0, 0 ).toInstant ( ZoneOffset.UTC ) );
                finfo.setConfiguration ( true );
            } );

            // builder.addRequirement ( "foo", "1.0", RpmDependencyFlags.EQUAL, RpmDependencyFlags.GREATER );

            outFile = builder.getTargetFile ();

            builder.build ();
        }

        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( Files.newInputStream ( outFile ) ) ) )
        {
            Dumper.dumpAll ( in );
        }
    }
}
