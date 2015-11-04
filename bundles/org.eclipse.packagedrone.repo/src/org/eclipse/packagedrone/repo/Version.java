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
package org.eclipse.packagedrone.repo;

public class Version implements Comparable<Version>
{
    private final org.osgi.framework.Version version;

    public static final Version EMPTY = new Version ( org.osgi.framework.Version.emptyVersion );

    public Version ( final int major, final int minor, final int micro, final String qualifier )
    {
        this.version = new org.osgi.framework.Version ( major, minor, micro, qualifier );
    }

    public Version ( final int major, final int minor, final int micro )
    {
        this.version = new org.osgi.framework.Version ( major, minor, micro );
    }

    public Version ( final String version )
    {
        this.version = new org.osgi.framework.Version ( version );
    }

    private Version ( final org.osgi.framework.Version version )
    {
        this.version = version;
    }

    public int getMajor ()
    {
        return this.version.getMajor ();
    }

    public int getMinor ()
    {
        return this.version.getMinor ();
    }

    public int getMicro ()
    {
        return this.version.getMicro ();
    }

    public String getQualifier ()
    {
        return this.version.getQualifier ();
    }

    public boolean isEmpty ()
    {
        // ignore the qualifier for this
        return this.version.getMajor () == 0 && this.version.getMinor () == 0 && this.version.getMicro () == 0;
    }

    @Override
    public String toString ()
    {
        return this.version.toString ();
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.version == null ? 0 : this.version.hashCode () );
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
        if ( ! ( obj instanceof Version ) )
        {
            return false;
        }
        final Version other = (Version)obj;
        if ( this.version == null )
        {
            if ( other.version != null )
            {
                return false;
            }
        }
        else if ( !this.version.equals ( other.version ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo ( final Version o )
    {
        return this.version.compareTo ( o.version );
    }

    public static Version valueOf ( final String value )
    {
        if ( value == null || value.isEmpty () )
        {
            return EMPTY;
        }
        return new Version ( org.osgi.framework.Version.valueOf ( value ) );
    }
}
