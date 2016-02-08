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
package org.eclipse.packagedrone.repo.channel.search;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

abstract class AbstractCompositePredicate implements CompositePredicate
{
    private final List<Predicate> predicates;

    AbstractCompositePredicate ( final Collection<Predicate> predicates )
    {
        Objects.requireNonNull ( predicates );

        this.predicates = unmodifiableList ( new ArrayList<> ( predicates ) );
    }

    AbstractCompositePredicate ( final Predicate... predicates )
    {
        Objects.requireNonNull ( predicates );

        this.predicates = unmodifiableList ( new ArrayList<> ( Arrays.asList ( predicates ) ) );
    }

    @Override
    public List<Predicate> getPredicates ()
    {
        return this.predicates;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.predicates == null ? 0 : this.predicates.hashCode () );
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
        if ( getClass () != obj.getClass () )
        {
            return false;
        }
        final AbstractCompositePredicate other = (AbstractCompositePredicate)obj;
        if ( this.predicates == null )
        {
            if ( other.predicates != null )
            {
                return false;
            }
        }
        else if ( !this.predicates.equals ( other.predicates ) )
        {
            return false;
        }
        return true;
    }

}
