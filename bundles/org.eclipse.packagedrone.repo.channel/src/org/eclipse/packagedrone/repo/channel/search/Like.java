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

public final class Like implements Predicate
{
    private final Value value;

    private final Literal pattern;

    private final boolean caseSensitive;

    Like ( final Value value, final Literal pattern, final boolean caseSensitive )
    {
        this.value = value;
        this.pattern = pattern;
        this.caseSensitive = caseSensitive;
    }

    public Value getValue ()
    {
        return this.value;
    }

    public Literal getPattern ()
    {
        return this.pattern;
    }

    public boolean isCaseSensitive ()
    {
        return this.caseSensitive;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.caseSensitive ? 1231 : 1237 );
        result = prime * result + ( this.pattern == null ? 0 : this.pattern.hashCode () );
        result = prime * result + ( this.value == null ? 0 : this.value.hashCode () );
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
        final Like other = (Like)obj;
        if ( this.caseSensitive != other.caseSensitive )
        {
            return false;
        }
        if ( this.pattern == null )
        {
            if ( other.pattern != null )
            {
                return false;
            }
        }
        else if ( !this.pattern.equals ( other.pattern ) )
        {
            return false;
        }
        if ( this.value == null )
        {
            if ( other.value != null )
            {
                return false;
            }
        }
        else if ( !this.value.equals ( other.value ) )
        {
            return false;
        }
        return true;
    }

}
