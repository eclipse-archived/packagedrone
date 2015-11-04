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
package org.eclipse.packagedrone.utils.rpm.tests;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.eclipse.packagedrone.utils.rpm.RpmBaseTag;
import org.eclipse.packagedrone.utils.rpm.RpmDependencyFlags;
import org.eclipse.packagedrone.utils.rpm.RpmHeader;
import org.eclipse.packagedrone.utils.rpm.RpmInputStream;
import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.RpmTagValue;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.junit.Assert;
import org.junit.Test;

public class InputStreamTest
{
    private void dumpAll ( final RpmInputStream in ) throws IOException
    {
        final RpmLead lead = in.getLead ();
        System.out.format ( "Version: %s.%s%n", lead.getMajor (), lead.getMinor () );
        System.out.format ( "Name: %s%n", lead.getName () );
        System.out.format ( "Signature Version: %s%n", lead.getSignatureVersion () );

        dumpHeader ( "Signature", in.getSignatureHeader (), tag -> RpmSignatureTag.find ( tag ) );
        dumpHeader ( "Payload", in.getPayloadHeader (), tag -> RpmTag.find ( tag ) );

        @SuppressWarnings ( "resource" )
        final CpioArchiveInputStream cpio = in.getCpioStream ();

        CpioArchiveEntry entry;
        while ( ( entry = cpio.getNextCPIOEntry () ) != null )
        {
            dumpEntry ( entry );
        }

        {
            final String[] names = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.REQUIRE_NAME ) ).asStringArray ().orElse ( null );
            final String[] versions = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.REQUIRE_VERSION ) ).asStringArray ().orElse ( null );
            final Long[] flags = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.REQUIRE_FLAGS ) ).asLongArray ().orElse ( null );
            dumpDeps ( "Require", names, versions, flags );
        }
        {
            final String[] names = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.PROVIDE_NAME ) ).asStringArray ().orElse ( null );
            final String[] versions = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.PROVIDE_VERSION ) ).asStringArray ().orElse ( null );
            final Long[] flags = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.PROVIDE_FLAGS ) ).asLongArray ().orElse ( null );
            dumpDeps ( "Provide", names, versions, flags );
        }
        {
            final String[] names = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.CONFLICT_NAME ) ).asStringArray ().orElse ( null );
            final String[] versions = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.CONFLICT_VERSION ) ).asStringArray ().orElse ( null );
            final Long[] flags = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.CONFLICT_FLAGS ) ).asLongArray ().orElse ( null );
            dumpDeps ( "Conflict", names, versions, flags );
        }
        {
            final String[] names = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.OBSOLETE_NAME ) ).asStringArray ().orElse ( null );
            final String[] versions = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.OBSOLETE_VERSION ) ).asStringArray ().orElse ( null );
            final Long[] flags = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.OBSOLETE_FLAGS ) ).asLongArray ().orElse ( null );
            dumpDeps ( "Obsolete", names, versions, flags );
        }
    }

    @Test
    public void test1 () throws IOException
    {
        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( new FileInputStream ( new File ( "data/org.eclipse.scada-0.2.1-1.noarch.rpm" ) ) ) ) )
        {
            dumpAll ( in );

            Assert.assertEquals ( 280, in.getPayloadHeader ().getStart () );
            Assert.assertEquals ( 3501, in.getPayloadHeader ().getLength () );

            Assert.assertEquals ( "cpio", in.getPayloadHeader ().getTag ( RpmTag.PAYLOAD_FORMAT ) );
            Assert.assertEquals ( "lzma", in.getPayloadHeader ().getTag ( RpmTag.PAYLOAD_CODING ) );

            Assert.assertEquals ( "org.eclipse.scada", in.getPayloadHeader ().getTag ( RpmTag.NAME ) );
            Assert.assertEquals ( "0.2.1", in.getPayloadHeader ().getTag ( RpmTag.VERSION ) );
            Assert.assertEquals ( "1", in.getPayloadHeader ().getTag ( RpmTag.RELEASE ) );

            Assert.assertEquals ( "noarch", in.getPayloadHeader ().getTag ( RpmTag.ARCH ) );
            Assert.assertEquals ( "linux", in.getPayloadHeader ().getTag ( RpmTag.OS ) );
            Assert.assertEquals ( "EPL", in.getPayloadHeader ().getTag ( RpmTag.LICENSE ) );

            Assert.assertArrayEquals ( new String[] { //
                    "/etc/", //
                    "/etc/eclipsescada/", //
                    "/etc/profile.d/", //
                    "/usr/bin/", //
                    "/usr/", //
                    "/usr/share/", //
                    "/usr/share/eclipsescada/", //
                    "/usr/share/eclipsescada/sql/", //
                    "/var/log/", //
                    "/var/run/", //
            }, (String[])in.getPayloadHeader ().getTag ( RpmTag.DIRNAMES ) );
        }
    }

    @Test
    public void test2 () throws IOException
    {
        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( new FileInputStream ( new File ( "data/org.eclipse.scada-centos6-0.2.1-1.noarch.rpm" ) ) ) ) )
        {
            dumpAll ( in );

        }
    }

    private void dumpDeps ( final String string, final String[] names, final String[] versions, final Long[] flags )
    {
        if ( names == null )
        {
            return;
        }

        for ( int i = 0; i < names.length; i++ )
        {
            System.out.format ( "%s: %s - %s - %s %s%n", string, names[i], versions[i], flags[i], RpmDependencyFlags.parse ( flags[i] ) );
        }
    }

    private void dumpHeader ( final String string, final RpmHeader<? extends RpmBaseTag> header, final Function<Integer, Object> func )
    {
        System.out.println ( string );
        System.out.println ( "=================================" );

        for ( final Map.Entry<Integer, Object> entry : new TreeMap<> ( header.getRawTags () ).entrySet () )
        {
            Object tag = func.apply ( entry.getKey () );
            if ( tag == null )
            {
                tag = entry.getKey ();
            }
            System.out.format ( "%20s - %s%n", tag, Rpms.dumpValue ( entry.getValue () ) );
        }
    }

    private void dumpEntry ( final CpioArchiveEntry entry )
    {
        System.out.format ( "-----------------------------------%n" );
        System.out.format ( " %s%n", entry.getName () );
        System.out.format ( " Size: %s%n", entry.getSize () );
    }
}
