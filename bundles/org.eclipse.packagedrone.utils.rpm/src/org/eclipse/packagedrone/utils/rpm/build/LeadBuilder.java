/*******************************************************************************
 * Copyright (c) 2016, 2018 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - allow arch/os mapping
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.packagedrone.utils.rpm.Architecture;
import org.eclipse.packagedrone.utils.rpm.OperatingSystem;
import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.RpmVersion;
import org.eclipse.packagedrone.utils.rpm.Type;
import org.eclipse.packagedrone.utils.rpm.header.Header;

public class LeadBuilder
{
    private String name;

    private RpmVersion version;

    private Type type = Type.BINARY;

    private short architecture;

    private short operatingSystem;

    public LeadBuilder ()
    {
    }

    public LeadBuilder ( final String name, final RpmVersion version )
    {
        this.name = name;
        this.version = version;
    }

    public void setType ( final Type type )
    {
        this.type = type;
    }

    public String getName ()
    {
        return this.name;
    }

    public Type getType ()
    {
        return this.type;
    }

    public RpmVersion getVersion ()
    {
        return this.version;
    }

    public short getArchitecture ()
    {
        return this.architecture;
    }

    public short getOperatingSystem ()
    {
        return this.operatingSystem;
    }

    public void fillFlagsFromHeader ( final Header<RpmTag> header, final Function<String, Optional<Architecture>> architectureMapper, final Function<String, Optional<OperatingSystem>> operatingSystemMapper )
    {
        Objects.requireNonNull ( header );
        Objects.requireNonNull ( architectureMapper );
        Objects.requireNonNull ( operatingSystemMapper );

        final Object os = header.get ( RpmTag.OS );
        final Object arch = header.get ( RpmTag.ARCH );

        if ( os instanceof String )
        {
            this.architecture = architectureMapper.apply ( (String)os ).orElse ( Architecture.NOARCH ).getValue ();
        }
        if ( arch instanceof String )
        {
            this.operatingSystem = operatingSystemMapper.apply ( (String)arch ).orElse ( OperatingSystem.UNKNOWN ).getValue ();
        }
    }

    public void fillFlagsFromHeader ( final Header<RpmTag> header )
    {
        fillFlagsFromHeader ( header, Architecture::fromAlias, OperatingSystem::fromAlias );
    }

    public RpmLead build ()
    {
        if ( this.name == null || this.name.isEmpty () )
        {
            throw new IllegalStateException ( "A name must be set" );
        }
        if ( this.version == null )
        {
            throw new IllegalStateException ( "A version must be set" );
        }
        return new RpmLead ( (byte)3, (byte)0, RpmLead.toLeadName ( this.name, this.version ), 5, this.type.getValue (), this.architecture, this.operatingSystem );
    }
}
