/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm;

import java.util.Objects;

public class RpmLead
{
    private final byte major;

    private final byte minor;

    private final String name;

    private final int signatureVersion;

    private final short type;

    private final short architecture;

    private final short operatingSystem;

    public RpmLead ( final byte major, final byte minor, final String name, final int signatureVersion, final short type, final short architecture, final short operatingSystem )
    {
        Objects.requireNonNull ( name );

        this.major = major;
        this.minor = minor;
        this.name = name;
        this.signatureVersion = signatureVersion;
        this.type = type;
        this.architecture = architecture;
        this.operatingSystem = operatingSystem;
    }

    public byte getMajor ()
    {
        return this.major;
    }

    public byte getMinor ()
    {
        return this.minor;
    }

    public String getName ()
    {
        return this.name;
    }

    public int getSignatureVersion ()
    {
        return this.signatureVersion;
    }

    public short getType ()
    {
        return this.type;
    }

    public short getArchitecture ()
    {
        return this.architecture;
    }

    public short getOperatingSystem ()
    {
        return this.operatingSystem;
    }

    public static String toLeadName ( final String packageName, final RpmVersion version )
    {
        Objects.requireNonNull ( packageName );
        Objects.requireNonNull ( version );

        final StringBuilder builder = new StringBuilder ();

        builder.append ( packageName );

        builder.append ( '-' ).append ( version.getVersion () );

        if ( version.getRelease ().isPresent () )
        {
            builder.append ( '-' ).append ( version.getRelease () );
        }

        return builder.toString ();

    }
}
