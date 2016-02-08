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

import java.util.Objects;

import org.eclipse.packagedrone.repo.MetaKey;

public final class MetaKeyValue implements Value
{
    private final MetaKey metaKey;

    MetaKeyValue ( final MetaKey metaKey )
    {
        Objects.requireNonNull ( metaKey );

        this.metaKey = metaKey;
    }

    public MetaKey getMetaKey ()
    {
        return this.metaKey;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.metaKey == null ? 0 : this.metaKey.hashCode () );
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
        final MetaKeyValue other = (MetaKeyValue)obj;
        if ( this.metaKey == null )
        {
            if ( other.metaKey != null )
            {
                return false;
            }
        }
        else if ( !this.metaKey.equals ( other.metaKey ) )
        {
            return false;
        }
        return true;
    }
}
