/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S2162
 *******************************************************************************/
package org.eclipse.packagedrone.repo.cleanup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultKey implements Comparable<ResultKey>
{
    private final List<String> keys;

    public ResultKey ( final List<String> keys )
    {
        this.keys = Collections.unmodifiableList ( new ArrayList<> ( keys ) );
    }

    public List<String> getKeys ()
    {
        return this.keys;
    }

    @Override
    public int compareTo ( final ResultKey o )
    {
        final int max = Math.min ( this.keys.size (), o.keys.size () );

        for ( int i = 0; i < max; i++ )
        {
            final String s1 = this.keys.get ( i );
            final String s2 = o.keys.get ( i );

            if ( s1 == null && s2 == null )
            {
                continue;
            }
            if ( s1 == null )
            {
                return -1;
            }
            if ( s2 == null )
            {
                return 1;
            }

            final int rc = s1.compareTo ( s2 );
            if ( rc != 0 )
            {
                return rc;
            }
        }

        if ( this.keys.size () == o.keys.size () )
        {
            // both lists also have equal size
            return 0;
        }

        if ( this.keys.size () == max )
        {
            // we are shorter, so show us first
            return 1;
        }
        else
        {
            return 1;
        }
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.keys == null ? 0 : this.keys.hashCode () );
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
        if ( this.getClass() != obj.getClass() )
        {
            return false;
        }
        final ResultKey other = (ResultKey)obj;
        if ( this.keys == null )
        {
            if ( other.keys != null )
            {
                return false;
            }
        }
        else if ( !this.keys.equals ( other.keys ) )
        {
            return false;
        }
        return true;
    }

}
