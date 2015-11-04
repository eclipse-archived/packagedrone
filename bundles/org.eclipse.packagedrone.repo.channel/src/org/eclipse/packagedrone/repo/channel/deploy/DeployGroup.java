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
package org.eclipse.packagedrone.repo.channel.deploy;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class DeployGroup
{
    public static final Comparator<DeployGroup> NAME_COMPARATOR = nullsFirst ( comparing ( DeployGroup::getName ) );

    private final String id;

    private final String name;

    private final List<DeployKey> keys;

    public DeployGroup ( final String id, final String name, final List<DeployKey> keys )
    {
        this.id = id;
        this.name = name;
        this.keys = keys != null ? new CopyOnWriteArrayList<> ( keys ) : Collections.emptyList ();
    }

    public DeployGroup ( final String id, final String name, final List<DeployKey> keys, final Function<DeployGroup, Collection<DeployKey>> keyGenerator )
    {
        this.id = id;
        this.name = name;
        this.keys = keys != null ? new CopyOnWriteArrayList<> ( keys ) : new CopyOnWriteArrayList<> ();

        final Collection<DeployKey> result = keyGenerator.apply ( this );
        if ( result != null )
        {
            this.keys.addAll ( result );
        }
    }

    public String getId ()
    {
        return this.id;
    }

    public String getName ()
    {
        return this.name;
    }

    public List<DeployKey> getKeys ()
    {
        return Collections.unmodifiableList ( this.keys );
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( ! ( obj instanceof DeployGroup ) )
        {
            return false;
        }
        final DeployGroup other = (DeployGroup)obj;
        if ( this.id == null )
        {
            if ( other.id != null )
            {
                return false;
            }
        }
        else if ( !this.id.equals ( other.id ) )
        {
            return false;
        }
        return true;
    }
}
