/*******************************************************************************
 * Copyright (c) 2016, 2018 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - add new aliases
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Architecture
{
    NOARCH ( (short)0, "noarch" ),
    INTEL ( (short)1, "athlon, geode, pentium3, pentium4, i386, i486, i586, i686, x86_64, amd64, ia32e, em64t" ),
    ALPHA ( (short)2, "alpha, alphaev5, alphaev56, alphapca56, alphaev6, alphaev67" ),
    SPARC ( (short)3, "sparc, sun4, sun4c, sun4d, sun4m, sparcv8, sparcv9, sparcv9v" ),
    MIPS ( (short)4, "mips" ),
    PPC ( (short)5, "ppc, ppc8260, ppc8560, ppc32dy4, ppciseries, ppcpseries" ),
    M86K ( (short)6, "m68k" ),
    IP ( (short)7, "sgi" ),
    RS6000 ( (short)8, "rs6000" ),
    IA64 ( (short)9, "ia64" ),
    MIPSEL ( (short)11, "mipsel" ),
    ARM ( (short)12, "armv3l, armv4b, armv4l, armv5tel, armv5tejl, armv6l, armv6hl, armv7l, armv7hl" ),
    M86KMINT ( (short)13, "m68kmint, atarist, atariste, ataritt, falcon, atariclone, milan, hades" ),
    S390 ( (short)14, "s390, i370" ),
    S390X ( (short)15, "s390x" ),
    PPC64 ( (short)16, "ppc64, ppc64iseries, ppc64pseries, ppc64p7" ),
    SH ( (short)17, "sh, sh3, sh4, sh4a" ),
    XTENSA ( (short)18, "xtensa" ),
    AARCH64 ( (short)19, "aarch64" );

    private static final Map<Integer, Architecture> MAP = new HashMap<> ();

    private static final Map<String, Architecture> ALTMAP = new HashMap<> ();

    static
    {
        for ( final Architecture arch : Architecture.values () )
        {
            MAP.put ( (int)arch.value, arch );
            ALTMAP.put ( arch.name ().toLowerCase (), arch );
            for ( final String alias : getAliases ( arch ).split ( ",\\s" ) )
            {
                ALTMAP.put ( alias.toLowerCase (), arch );
            }
        }
    }

    private static String getAliases ( final Architecture arch )
    {
        return System.getProperty ( Architecture.class.getPackage ().getName () + ".arch." + arch.name (), arch.aliases );
    }

    private short value;

    private String aliases;

    private Architecture ( final short value, final String aliases )
    {
        this.value = value;
        this.aliases = aliases;
    }

    public short getValue ()
    {
        return this.value;
    }

    public static Optional<Architecture> fromValue ( final int value )
    {
        return Optional.ofNullable ( MAP.get ( value ) );
    }

    public static Optional<Architecture> fromAlias ( final String alias )
    {
        if ( alias == null )
        {
            return Optional.empty ();
        }

        return Optional.ofNullable ( ALTMAP.get ( alias.toLowerCase () ) );
    }
}
