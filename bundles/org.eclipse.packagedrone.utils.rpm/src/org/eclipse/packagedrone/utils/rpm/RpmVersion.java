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
import java.util.Optional;

public class RpmVersion
{
    private final Optional<Integer> epoch;

    private final String version;

    private final Optional<String> release;

    public RpmVersion ( final String version )
    {
        this ( version, null );
    }

    public RpmVersion ( final String version, final String release )
    {
        this ( null, version, release );
    }

    public RpmVersion ( final Integer epoch, final String version, final String release )
    {
        this.epoch = Optional.ofNullable ( epoch );
        this.version = Objects.requireNonNull ( version );
        this.release = Optional.ofNullable ( release );
    }

    public RpmVersion ( final Optional<Integer> epoch, final String version, final Optional<String> release )
    {
        this.epoch = Objects.requireNonNull ( epoch );
        this.version = Objects.requireNonNull ( version );
        this.release = Objects.requireNonNull ( release );
    }

    public Optional<Integer> getEpoch ()
    {
        return this.epoch;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public Optional<String> getRelease ()
    {
        return this.release;
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        this.epoch.ifPresent ( v -> sb.append ( v ).append ( ':' ) );

        sb.append ( this.version );

        if ( this.release.isPresent () && !this.release.get ().isEmpty () )
        {
            sb.append ( '-' ).append ( this.release.get () );
        }

        return sb.toString ();
    }

    public static RpmVersion valueOf ( final String version )
    {
        if ( version == null || version.isEmpty () )
        {
            return null;
        }

        final String[] toks1 = version.split ( ":", 2 );

        final String n;
        Integer epoch = null;
        if ( toks1.length > 1 )
        {
            epoch = Integer.parseInt ( toks1[0] );
            n = toks1[1];
        }
        else
        {
            n = toks1[0];
        }

        final String[] toks2 = n.split ( "-", 2 );

        final String ver = toks2[0];
        final String rel = toks2.length > 1 ? toks2[1] : null;

        return new RpmVersion ( epoch, ver, rel );
    }
}
