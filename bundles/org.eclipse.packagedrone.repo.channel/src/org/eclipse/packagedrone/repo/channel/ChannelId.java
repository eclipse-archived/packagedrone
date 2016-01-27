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
package org.eclipse.packagedrone.repo.channel;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class ChannelId
{
    private final String id;

    private final Set<String> names;

    private final String description;

    public ChannelId ( final String id )
    {
        this ( id, Collections.emptySet (), null );
    }

    public ChannelId ( final String id, final Set<String> names, final String description )
    {
        Objects.requireNonNull ( id );
        Objects.requireNonNull ( names );

        this.id = id;
        this.names = Collections.unmodifiableSet ( new LinkedHashSet<> ( names ) );

        if ( this.id == null )
        {
            throw new NullPointerException ( "'id' must not be null" );
        }

        this.description = description;
    }

    public String getId ()
    {
        return this.id;
    }

    public Set<String> getNames ()
    {
        return this.names;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public String makeTitle ()
    {
        final String desc = this.description;
        if ( desc != null && !desc.isEmpty () )
        {
            return String.format ( "%s (%s)", this.id, desc );
        }
        else
        {
            return this.id;
        }
    }

    // public static Comparator<? super ChannelId> NAME_COMPARATOR = Comparator.comparing ( ChannelId::getName, Comparator.nullsLast ( Comparator.naturalOrder () ) ).thenComparing ( Comparator.comparing ( ChannelId::getId ) );
}
