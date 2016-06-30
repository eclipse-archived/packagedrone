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
package org.eclipse.packagedrone.utils.rpm.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformation.Dependency;
import org.eclipse.packagedrone.utils.rpm.parse.InputHeader;
import org.eclipse.packagedrone.utils.rpm.parse.RpmInputStream;

public final class RpmInformations
{
    private RpmInformations ()
    {
    }

    public static RpmInformation makeInformation ( final RpmInputStream in ) throws IOException
    {
        final InputHeader<RpmTag> header = in.getPayloadHeader ();
        final InputHeader<RpmSignatureTag> signature = in.getSignatureHeader ();

        final RpmInformation result = new RpmInformation ();

        result.setHeaderStart ( header.getStart () );
        result.setHeaderEnd ( header.getStart () + header.getLength () );

        result.setName ( asString ( header.getTag ( RpmTag.NAME ) ) );
        result.setArchitecture ( asString ( header.getTag ( RpmTag.ARCH ) ) );
        result.setSummary ( asString ( header.getTag ( RpmTag.SUMMARY ) ) );
        result.setDescription ( asString ( header.getTag ( RpmTag.DESCRIPTION ) ) );
        result.setPackager ( asString ( header.getTag ( RpmTag.PACKAGER ) ) );
        result.setUrl ( asString ( header.getTag ( RpmTag.URL ) ) );
        result.setLicense ( asString ( header.getTag ( RpmTag.LICENSE ) ) );
        result.setVendor ( asString ( header.getTag ( RpmTag.VENDOR ) ) );
        result.setGroup ( asString ( header.getTag ( RpmTag.GROUP ) ) );

        result.setBuildHost ( asString ( header.getTag ( RpmTag.BUILDHOST ) ) );
        result.setBuildTimestamp ( asLong ( header.getTag ( RpmTag.BUILDTIME ) ) );
        result.setSourcePackage ( asString ( header.getTag ( RpmTag.SOURCE_PACKAGE ) ) );

        result.setInstalledSize ( asLong ( header.getTag ( RpmTag.SIZE ) ) );
        result.setArchiveSize ( asLong ( header.getTag ( RpmTag.ARCHIVE_SIZE ) ) );
        if ( result.getArchiveSize () == null )
        {
            result.setArchiveSize ( asLong ( signature.getTag ( RpmSignatureTag.PAYLOAD_SIZE ) ) );
        }

        // version

        final RpmInformation.Version ver = new RpmInformation.Version ( asString ( header.getTag ( RpmTag.VERSION ) ), asString ( header.getTag ( RpmTag.RELEASE ) ), asString ( header.getTag ( RpmTag.EPOCH ) ) );
        result.setVersion ( ver );

        // changelog

        final Object val = header.getTag ( RpmTag.CHANGELOG_TIMESTAMP );
        if ( val instanceof Long[] )
        {
            final Long[] ts = (Long[])val;
            final String[] authors = (String[])header.getTag ( RpmTag.CHANGELOG_AUTHOR );
            final String[] texts = (String[])header.getTag ( RpmTag.CHANGELOG_TEXT );

            final List<RpmInformation.Changelog> changes = new ArrayList<> ( ts.length );

            for ( int i = 0; i < ts.length; i++ )
            {
                changes.add ( new RpmInformation.Changelog ( ts[i], authors[i], texts[i] ) );
            }

            Collections.sort ( changes, ( o1, o2 ) -> Long.compare ( o1.getTimestamp (), o2.getTimestamp () ) );

            result.setChangelog ( changes );
        }

        // dependencies

        result.setProvides ( makeDependencies ( header, RpmTag.PROVIDE_NAME, RpmTag.PROVIDE_VERSION, RpmTag.PROVIDE_FLAGS ) );
        result.setRequires ( makeDependencies ( header, RpmTag.REQUIRE_NAME, RpmTag.REQUIRE_VERSION, RpmTag.REQUIRE_FLAGS ) );
        result.setConflicts ( makeDependencies ( header, RpmTag.CONFLICT_NAME, RpmTag.CONFLICT_VERSION, RpmTag.CONFLICT_FLAGS ) );
        result.setObsoletes ( makeDependencies ( header, RpmTag.OBSOLETE_NAME, RpmTag.OBSOLETE_VERSION, RpmTag.OBSOLETE_FLAGS ) );

        // files

        final CpioArchiveInputStream cpio = in.getCpioStream ();
        CpioArchiveEntry cpioEntry;
        while ( ( cpioEntry = cpio.getNextCPIOEntry () ) != null )
        {
            final String name = normalize ( cpioEntry.getName () );

            if ( cpioEntry.isRegularFile () )
            {
                result.getFiles ().add ( name );
            }
            else if ( cpioEntry.isDirectory () )
            {
                result.getDirectories ().add ( name );
            }
        }
        cpio.close ();

        return result;
    }

    public static List<Dependency> makeDependencies ( final InputHeader<RpmTag> header, final RpmTag namesTag, final RpmTag versionsTag, final RpmTag flagsTag )
    {
        Object namesVal = header.getTag ( namesTag );
        Object versionsVal = header.getTag ( versionsTag );
        Object flagsVal = header.getTag ( flagsTag );

        if ( namesVal == null || ! ( namesVal instanceof String[] ) )
        {
            if ( namesVal instanceof String )
            {
                namesVal = new String[] { (String)namesVal };
            }
            else
            {
                return Collections.emptyList ();
            }
        }

        if ( versionsVal != null && ! ( versionsVal instanceof String[] ) )
        {
            if ( versionsVal instanceof String )
            {
                versionsVal = new String[] { (String)versionsVal };
            }
            else
            {
                throw new IllegalStateException ( String.format ( "Invalid dependencies version format [%s]: %s", versionsTag, versionsVal ) );
            }
        }

        if ( flagsVal != null && ! ( flagsVal instanceof Long[] ) )
        {
            if ( flagsVal instanceof Long )
            {
                flagsVal = new Long[] { (Long)flagsVal };
            }
            else if ( flagsVal instanceof Integer )
            {
                flagsVal = new Long[] { ( (Integer)flagsVal ).longValue () };
            }
            else if ( flagsVal instanceof Integer[] )
            {
                flagsVal = Arrays.stream ( (Integer[])flagsVal ).map ( Integer::longValue ).toArray ( Long[]::new );
            }
            else
            {
                throw new IllegalStateException ( String.format ( "Invalid dependencies flags format [%s]: %s", flagsTag, flagsVal ) );
            }
        }

        final String[] names = (String[])namesVal;
        final String[] versions = (String[])versionsVal;
        final Long[] flags = (Long[])flagsVal;

        if ( versions != null && names.length != versions.length )
        {
            throw new IllegalStateException ( String.format ( "Invalid size of dependency versions array [%s] - expected: %s, actual: %s", versionsTag, names.length, versions.length ) );
        }

        if ( flags != null && names.length != flags.length )
        {
            throw new IllegalStateException ( String.format ( "Invalid size of dependency flags array [%s] - expected: %s, actual: %s", flagsTag, names.length, flags.length ) );
        }

        final List<Dependency> result = new ArrayList<> ( names.length );

        final Set<String> known = new HashSet<> ();

        for ( int i = 0; i < names.length; i++ )
        {
            final String name = names[i];
            String version = versions != null ? versions[i] : null;
            if ( version != null && version.isEmpty () )
            {
                version = null;
            }
            final Long flag = flags != null ? flags[i] : null;

            final String key = name; // for now the key is the name

            if ( known.add ( key ) )
            {
                result.add ( new Dependency ( name, version, flag != null ? flag : 0L ) );
            }
        }

        return result;
    }

    public static String normalize ( final String name )
    {
        if ( name.startsWith ( "./" ) )
        {
            return name.substring ( 1 );
        }

        return name;
    }

    public static String asString ( final Object value )
    {
        if ( value == null )
        {
            return null;
        }

        if ( value instanceof String )
        {
            return (String)value;
        }
        if ( value instanceof String[] )
        {
            final String[] values = (String[])value;
            if ( values.length > 0 )
            {
                return values[0];
            }
            return null;
        }

        return value.toString ();
    }

    public static Long asLong ( final Object value )
    {
        if ( value == null )
        {
            return null;
        }

        if ( value instanceof Number )
        {
            return ( (Number)value ).longValue ();
        }

        try
        {
            return Long.parseLong ( value.toString () );
        }
        catch ( final NumberFormatException e )
        {
            return null;
        }
    }

}
