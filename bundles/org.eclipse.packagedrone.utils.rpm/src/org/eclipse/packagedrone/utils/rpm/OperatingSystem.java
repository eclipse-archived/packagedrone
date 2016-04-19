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
package org.eclipse.packagedrone.utils.rpm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum OperatingSystem
{
    UNKNOWN ( (short)0, "unknown" ),
    LINUX ( (short)1, "Linux" ),
    IRIX ( (short)2, "Irix" ),
    SUNOS_5 ( (short)3, "solaris, SunOS5" ),
    SUNOS_4 ( (short)4, "SunOS, SunOS4" ),
    AIX ( (short)5, "AIX, AmigaOS" ),
    HP_UX ( (short)6, "hpux10, HP-UX" ),
    OSF ( (short)7, "OSF1, osf1, osf4.0, osf3.2" ),
    FREEBSD ( (short)8, "FreeBSD" ),
    IRIX64 ( (short)10, "Irix64" ),
    NEXTSTEP ( (short)11, "NextStep" ),
    BSD ( (short)12, "BSD_OS, bsdi" ),
    MACHTEN ( (short)13, "machten" ),
    CYGWIN32_NT ( (short)14, "cygwin32" ),
    CYGWIN32_95 ( (short)15, "" ),
    UNIX_SV ( (short)16, "MP_RAS" ),
    MINT ( (short)17, "MiNT, FreeMiNT" ),
    OS_390 ( (short)18, "OS/390" ),
    VM_ESA ( (short)19, "VM/ESA, Linux/ESA" ),
    LINUX_390 ( (short)20, "Linux/390, Linux/ESA" ),
    MACOS_X ( (short)21, "darwin, macosx" );

    private static final Map<Integer, OperatingSystem> MAP = new HashMap<> ();

    private static final Map<String, OperatingSystem> ALTMAP = new HashMap<> ();

    static
    {
        for ( final OperatingSystem os : OperatingSystem.values () )
        {
            MAP.put ( (int)os.value, os );
            ALTMAP.put ( os.name ().toLowerCase (), os );
            for ( final String alias : getAliases ( os ).split ( ",\\s" ) )
            {
                ALTMAP.put ( alias.toLowerCase (), os );
            }
        }
    }

    private static String getAliases ( final OperatingSystem os )
    {
        return System.getProperty ( OperatingSystem.class.getPackage ().getName () + ".os." + os.name (), os.aliases );
    }

    private short value;

    private String aliases;

    private OperatingSystem ( final short value, final String aliases )
    {
        this.value = value;
        this.aliases = aliases;
    }

    public short getValue ()
    {
        return this.value;
    }

    public static Optional<OperatingSystem> fromValue ( final int value )
    {
        return Optional.ofNullable ( MAP.get ( value ) );
    }

    public static Optional<OperatingSystem> fromAlias ( final String alias )
    {
        if ( alias == null )
        {
            return Optional.empty ();
        }

        return Optional.ofNullable ( ALTMAP.get ( alias.toLowerCase () ) );
    }
}
