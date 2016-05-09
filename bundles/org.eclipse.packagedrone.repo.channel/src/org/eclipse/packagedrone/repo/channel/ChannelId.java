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

    /**
     * Get the channel id
     * <p>
     * This is the internal, immutable ID of the channel
     * </p>
     *
     * @return the channel ID, never {@code null}
     */
    public String getId ()
    {
        return this.id;
    }

    /**
     * Get the alias names of the channel
     * <p>
     * Many operations do allows accessing the channel by using an alias,
     * instead of the internal ID. However the alias list can be changed and
     * aliases may be re-assigned to another channel. The result may be an empty
     * set.
     * </p>
     *
     * @return the set or alias names, never {@code null}
     */
    public Set<String> getNames ()
    {
        return this.names;
    }

    /**
     * Get the optional description
     *
     * @return the description, or {@code null} if none is set
     */
    public String getDescription ()
    {
        return this.description;
    }

    /**
     * Get the optional short description
     * <p>
     * The short description is the first line of {@link #getDescription()}
     * </p>
     * 
     * @return the short description, or {@code null} if none is set
     */
    public String getShortDescription ()
    {
        if ( this.description == null )
        {
            return null;
        }

        final String[] lines = this.description.split ( "\\R", 2 );
        if ( lines.length > 0 )
        {
            return lines[0];
        }
        else
        {
            return "";
        }
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
