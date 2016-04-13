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

import java.util.Arrays;
import java.util.EnumSet;

public class Dependency
{

    private final String name;

    private final String version;

    private final EnumSet<RpmDependencyFlags> flags;

    public Dependency ( final String name, final String version, final RpmDependencyFlags... flags )
    {
        this.name = name;
        this.version = version;
        this.flags = EnumSet.copyOf ( Arrays.asList ( flags ) );
    }

    public String getName ()
    {
        return this.name;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public EnumSet<RpmDependencyFlags> getFlags ()
    {
        return this.flags;
    }

    public boolean isRpmLib ()
    {
        return this.flags.contains ( RpmDependencyFlags.RPMLIB );
    }

    @Override
    public String toString ()
    {
        return String.format ( "[%s, %s, %s]", this.name, this.version, this.flags );
    }
}
