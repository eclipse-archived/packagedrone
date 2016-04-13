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

import java.util.Collection;

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

        Header.putFields ( header, dependencies, namesTag, String[]::new, Dependency::getName, Header::putStringArray );
        Header.putFields ( header, dependencies, versionsTag, String[]::new, Dependency::getVersion, Header::putStringArray );
        Header.putIntFields ( header, dependencies, flagsTag, dep -> RpmDependencyFlags.encode ( dep.getFlags () ) );
    }
}
