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
package org.eclipse.packagedrone.repo.adapter.maven.upload;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.packagedrone.repo.adapter.maven.upload.Uploader.NoParentMode;

/**
 * Options for the {@link Uploader}
 */
public class Options
{
    private final static Options DEFAULT_OPTIONS = new Options ();

    private Set<String> ignoreExtensions;

    private Set<String> checksumExtensions;

    private NoParentMode noParentMode;

    public Options ()
    {
        this ( DEFAULT_OPTIONS );
    }

    public Options ( final Options other )
    {
        if ( other != null )
        {
            this.ignoreExtensions = new HashSet<> ( other.ignoreExtensions );
            this.checksumExtensions = new HashSet<> ( other.checksumExtensions );
            this.noParentMode = other.noParentMode;
        }
        else
        {
            this.ignoreExtensions = new HashSet<> ();
            this.checksumExtensions = new HashSet<> ( Arrays.asList ( "md5", "sha1" ) );
            this.noParentMode = NoParentMode.IGNORE;
        }
    }

    public void setChecksumExtensions ( final Collection<String> checksumExtensions )
    {
        Objects.requireNonNull ( checksumExtensions, "checksumExtensions" );

        this.checksumExtensions = new HashSet<> ( checksumExtensions );
    }

    public Set<String> getChecksumExtensions ()
    {
        return this.checksumExtensions;
    }

    public void setIgnoreExtensions ( final Collection<String> ignoreExtensions )
    {
        Objects.requireNonNull ( ignoreExtensions, "ignoreExtensions" );

        this.ignoreExtensions = new HashSet<> ( ignoreExtensions );
    }

    public Set<String> getIgnoreExtensions ()
    {
        return this.ignoreExtensions;
    }

    public void setNoParentMode ( final NoParentMode noParentMode )
    {
        Objects.requireNonNull ( noParentMode, "noParentMode" );

        this.noParentMode = noParentMode;
    }

    public NoParentMode getNoParentMode ()
    {
        return this.noParentMode;
    }

}
