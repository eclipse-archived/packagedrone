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

public final class Equal implements Predicate
{
    private final Value value1;

    private final Value value2;

    Equal ( final Value value1, final Value value2 )
    {
        this.value1 = value1;
        this.value2 = value2;
    }

    public Value getValue1 ()
    {
        return this.value1;
    }

    public Value getValue2 ()
    {
        return this.value2;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.value1 == null ? 0 : this.value1.hashCode () );
        result = prime * result + ( this.value2 == null ? 0 : this.value2.hashCode () );
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
        final Equal other = (Equal)obj;
        if ( this.value1 == null )
        {
            if ( other.value1 != null )
            {
                return false;
            }
        }
        else if ( !this.value1.equals ( other.value1 ) )
        {
            return false;
        }
        if ( this.value2 == null )
        {
            if ( other.value2 != null )
            {
                return false;
            }
        }
        else if ( !this.value2.equals ( other.value2 ) )
        {
            return false;
        }
        return true;
    }

}
