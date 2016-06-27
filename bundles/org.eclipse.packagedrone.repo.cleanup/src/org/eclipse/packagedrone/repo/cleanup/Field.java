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

import org.eclipse.packagedrone.repo.MetaKey;

public class Field
{
    private MetaKey key;

    private Order order;

    public Field ( final MetaKey key, final Order order )
    {
        this.key = key;
        this.order = order;
    }

    public Field ( final String namespace, final String key )
    {
        this.key = new MetaKey ( namespace, key );
        this.order = Order.ASCENDING;
    }

    public Field ()
    {
    }

    public void setKey ( final MetaKey key )
    {
        this.key = key;
    }

    public MetaKey getKey ()
    {
        return this.key;
    }

    public void setOrder ( final Order order )
    {
        this.order = order;
    }

    public Order getOrder ()
    {
        return this.order;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.key == null ? 0 : this.key.hashCode () );
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
        final Field other = (Field)obj;
        if ( this.key == null )
        {
            if ( other.key != null )
            {
                return false;
            }
        }
        else if ( !this.key.equals ( other.key ) )
        {
            return false;
        }
        return true;
    }

    public int compare ( final String v1, final String v2 )
    {
        if ( v1 == null )
        {
            return -1;
        }
        if ( v2 == null )
        {
            return 1;
        }

        switch ( this.order )
        {
            case ASCENDING:
                return v1.compareTo ( v2 );
            case DESCENDING:
                return v2.compareTo ( v1 );
            default:
                throw new IllegalArgumentException ( String.format ( "Order %s is unkown", this.order ) );
        }
    }

}
