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
package org.eclipse.packagedrone.utils.rpm.deps;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.packagedrone.utils.rpm.ReadableHeader;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.header.Header;

public final class Dependencies
{
    private Dependencies ()
    {
    }

    public static void putRequirements ( final Header<RpmTag> header, final Collection<Dependency> requirements )
    {
        putDependencies ( header, requirements, RpmTag.REQUIRE_NAME, RpmTag.REQUIRE_VERSION, RpmTag.REQUIRE_FLAGS );
    }

    public static void putProvides ( final Header<RpmTag> header, final Collection<Dependency> provides )
    {
        putDependencies ( header, provides, RpmTag.PROVIDE_NAME, RpmTag.PROVIDE_VERSION, RpmTag.PROVIDE_FLAGS );
    }

    public static void putConflicts ( final Header<RpmTag> header, final Collection<Dependency> conflicts )
    {
        putDependencies ( header, conflicts, RpmTag.CONFLICT_NAME, RpmTag.CONFLICT_VERSION, RpmTag.CONFLICT_FLAGS );
    }

    public static void putObsoletes ( final Header<RpmTag> header, final Collection<Dependency> obsoletes )
    {
        putDependencies ( header, obsoletes, RpmTag.OBSOLETE_NAME, RpmTag.OBSOLETE_VERSION, RpmTag.OBSOLETE_FLAGS );
    }

    private static void putDependencies ( final Header<RpmTag> header, final Collection<Dependency> dependencies, final RpmTag namesTag, final RpmTag versionsTag, final RpmTag flagsTag )
    {
        if ( dependencies.isEmpty () )
        {
            return;
        }

        // first sort

        final List<Dependency> deps = new ArrayList<> ( dependencies );
        Collections.sort ( deps, comparing ( Dependency::getName ).thenComparing ( comparing ( Dependency::getVersion, nullsFirst ( naturalOrder () ) ) ) );

        // then set

        Header.putFields ( header, deps, namesTag, String[]::new, Dependency::getName, Header::putStringArray );
        Header.putFields ( header, deps, versionsTag, String[]::new, Dependency::getVersion, Header::putStringArray );
        Header.putIntFields ( header, deps, flagsTag, dep -> RpmDependencyFlags.encode ( dep.getFlags () ) );
    }

    public static List<Dependency> getRequirements ( final ReadableHeader<RpmTag> header )
    {
        return getDependencies ( header, RpmTag.REQUIRE_NAME, RpmTag.REQUIRE_VERSION, RpmTag.REQUIRE_FLAGS );
    }

    public static List<Dependency> getProvides ( final ReadableHeader<RpmTag> header )
    {
        return getDependencies ( header, RpmTag.PROVIDE_NAME, RpmTag.PROVIDE_VERSION, RpmTag.PROVIDE_FLAGS );
    }

    public static List<Dependency> getConflicts ( final ReadableHeader<RpmTag> header )
    {
        return getDependencies ( header, RpmTag.CONFLICT_NAME, RpmTag.CONFLICT_VERSION, RpmTag.CONFLICT_FLAGS );
    }

    public static List<Dependency> getObsoletes ( final ReadableHeader<RpmTag> header )
    {
        return getDependencies ( header, RpmTag.OBSOLETE_NAME, RpmTag.OBSOLETE_VERSION, RpmTag.OBSOLETE_FLAGS );
    }

    private static List<Dependency> getDependencies ( final ReadableHeader<RpmTag> header, final RpmTag namesTag, final RpmTag versionsTag, final RpmTag flagsTag )
    {
        Objects.requireNonNull ( header );

        final Object rawNames = header.getValue ( namesTag ).orElse ( null );
        final Object rawVersions = header.getValue ( versionsTag ).orElse ( null );
        final Object rawFlags = header.getValue ( flagsTag ).orElse ( null );

        if ( rawNames instanceof String[] && rawVersions instanceof String[] && rawFlags instanceof int[] )
        {
            final String[] names = (String[])rawNames;
            final String[] versions = (String[])rawVersions;
            final int[] flags = (int[])rawFlags;

            if ( names.length == versions.length && names.length == flags.length )
            {
                final List<Dependency> result = new ArrayList<> ( names.length );
                for ( int i = 0; i < names.length; i++ )
                {
                    final String name = names[i];
                    final String version = versions[i];
                    final EnumSet<RpmDependencyFlags> flagSet = RpmDependencyFlags.parse ( flags[i] );
                    result.add ( new Dependency ( name, version, flagSet ) );
                }
                return result;
            }
        }

        return new LinkedList<> ();
    }
}
