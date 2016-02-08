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

public class Not implements Predicate
{
    private final Predicate predicate;

    Not ( final Predicate predicate )
    {
        this.predicate = predicate;
    }

    public Predicate getPredicate ()
    {
        return this.predicate;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.predicate == null ? 0 : this.predicate.hashCode () );
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
        final Not other = (Not)obj;
        if ( this.predicate == null )
        {
            if ( other.predicate != null )
            {
                return false;
            }
        }
        else if ( !this.predicate.equals ( other.predicate ) )
        {
            return false;
        }
        return true;
    }
}
