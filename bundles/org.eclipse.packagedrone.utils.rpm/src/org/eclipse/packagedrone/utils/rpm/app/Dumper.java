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
package org.eclipse.packagedrone.utils.rpm.app;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.eclipse.packagedrone.utils.rpm.RpmBaseTag;
import org.eclipse.packagedrone.utils.rpm.RpmEntry;
import org.eclipse.packagedrone.utils.rpm.RpmHeader;
import org.eclipse.packagedrone.utils.rpm.RpmInputStream;
import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.RpmTagValue;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.eclipse.packagedrone.utils.rpm.deps.RpmDependencyFlags;

public class Dumper
{
    public static void dumpAll ( final RpmInputStream in ) throws IOException
    {
        final RpmLead lead = in.getLead ();
        System.out.format ( "Version: %s.%s%n", lead.getMajor (), lead.getMinor () );
        System.out.format ( "Name: %s%n", lead.getName () );
        System.out.format ( "Signature Version: %s%n", lead.getSignatureVersion () );

        dumpHeader ( "Signature", in.getSignatureHeader (), tag -> RpmSignatureTag.find ( tag ), false );
        dumpHeader ( "Payload", in.getPayloadHeader (), tag -> RpmTag.find ( tag ), false );

        final CpioArchiveInputStream cpio = in.getCpioStream ();

        CpioArchiveEntry entry;
        while ( ( entry = cpio.getNextCPIOEntry () ) != null )
        {
            dumpEntry ( entry );
        }

        {
            final String[] names = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.REQUIRE_NAME ) ).asStringArray ().orElse ( null );
            final String[] versions = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.REQUIRE_VERSION ) ).asStringArray ().orElse ( null );
            final Integer[] flags = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.REQUIRE_FLAGS ) ).asIntegerArray ().orElse ( null );
            dumpDeps ( "Require", names, versions, flags );
        }
        {
            final String[] names = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.PROVIDE_NAME ) ).asStringArray ().orElse ( null );
            final String[] versions = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.PROVIDE_VERSION ) ).asStringArray ().orElse ( null );
            final Integer[] flags = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.PROVIDE_FLAGS ) ).asIntegerArray ().orElse ( null );
            dumpDeps ( "Provide", names, versions, flags );
        }
        {
            final String[] names = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.CONFLICT_NAME ) ).asStringArray ().orElse ( null );
            final String[] versions = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.CONFLICT_VERSION ) ).asStringArray ().orElse ( null );
            final Integer[] flags = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.CONFLICT_FLAGS ) ).asIntegerArray ().orElse ( null );
            dumpDeps ( "Conflict", names, versions, flags );
        }
        {
            final String[] names = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.OBSOLETE_NAME ) ).asStringArray ().orElse ( null );
            final String[] versions = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.OBSOLETE_VERSION ) ).asStringArray ().orElse ( null );
            final Integer[] flags = new RpmTagValue ( in.getPayloadHeader ().getTag ( RpmTag.OBSOLETE_FLAGS ) ).asIntegerArray ().orElse ( null );
            dumpDeps ( "Obsolete", names, versions, flags );
        }
    }

    private static void dumpDeps ( final String string, final String[] names, final String[] versions, final Integer[] flags )
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

    private static void dumpHeader ( final String string, final RpmHeader<? extends RpmBaseTag> header, final Function<Integer, Object> func, final boolean sorted )
    {
        System.out.println ( string );
        System.out.println ( "=================================" );

        Set<Entry<Integer, RpmEntry>> data;
        if ( sorted )
        {
            data = new TreeMap<> ( header.getRawTags () ).entrySet ();
        }
        else
        {
            data = header.getRawTags ().entrySet ();
        }

        for ( final Map.Entry<Integer, RpmEntry> entry : data )
        {
            Object tag = func.apply ( entry.getKey () );
            if ( tag == null )
            {
                tag = entry.getKey ();
            }

            System.out.format ( "%20s - %s%n", tag, Rpms.dumpValue ( entry.getValue () ) );

            if ( entry.getKey () == 62 || entry.getKey () == 63 )
            {
                final ByteBuffer buf = ByteBuffer.wrap ( (byte[])entry.getValue ().getValue () );
                System.out.format ( "Immutable - tag: %s, type: %s, position: %s, count: %s%n", buf.getInt (), buf.getInt (), buf.getInt (), buf.getInt () );
            }
        }
    }

    private static void dumpEntry ( final CpioArchiveEntry entry )
    {
        System.out.format ( "-----------------------------------%n" );
        System.out.format ( " %s%n", entry.getName () );
        System.out.format ( " Size: %s, Chksum: %016x, Align: %s, Inode: %016x, Mode: %08o, NoL: %s, Device: %s.%s%n", entry.getSize (), entry.getChksum (), entry.getAlignmentBoundary (), entry.getInode (), entry.getMode (), entry.getNumberOfLinks (), entry.getDeviceMaj (), entry.getDeviceMin () );
    }

    public static void main ( final String[] args ) throws IOException
    {
        for ( final String file : args )
        {
            dump ( Paths.get ( file ) );
        }
    }

    private static void dump ( final Path path ) throws IOException
    {
        if ( !Files.exists ( path ) )
        {
            System.err.format ( "%s: does not exist%n" );
            return;
        }

        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( Files.newInputStream ( path ) ) ) )
        {
            Dumper.dumpAll ( in );
        }
        catch ( final Exception e )
        {
            System.err.format ( "%s: failed to read file%n" );
            e.printStackTrace ( System.err );
        }
    }
}
