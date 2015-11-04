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
package org.eclipse.packagedrone.utils.rpm;

import java.util.EnumSet;

public enum RpmDependencyFlags
{
    LESS ( 1 ),
    GREATER ( 2 ),
    EQUAL ( 3 ),
    PREREQ ( 6 ),
    SCRIPT_PRE ( 9 ),
    SCRIPT_POST ( 10 ),
    RPMLIB ( 24 );

    private int value;

    private RpmDependencyFlags ( final int bit )
    {
        this.value = 1 << bit;
    }

    public static EnumSet<RpmDependencyFlags> parse ( final Long flags )
    {
        if ( flags == null )
        {
            return null;
        }

        return parse ( flags.intValue () );
    }

    public static EnumSet<RpmDependencyFlags> parse ( final int flags )
    {
        final EnumSet<RpmDependencyFlags> result = EnumSet.noneOf ( RpmDependencyFlags.class );

        for ( final RpmDependencyFlags f : values () )
        {
            if ( ( flags & f.value ) > 0 )
            {
                result.add ( f );
            }
        }

        return result;
    }
}
