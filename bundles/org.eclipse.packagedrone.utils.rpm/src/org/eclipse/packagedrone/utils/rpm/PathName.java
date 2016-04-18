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
package org.eclipse.packagedrone.utils.rpm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PathName implements Comparable<PathName>
{
    private final String[] segments;

    public PathName ( final String[] segments )
    {
        Objects.requireNonNull ( segments );

        this.segments = Arrays.copyOf ( segments, segments.length );
    }

    public PathName ( final List<String> segments )
    {
        Objects.requireNonNull ( segments );

        this.segments = segments.toArray ( new String[segments.size ()] );
    }

    public String[] getSegments ()
    {
        return this.segments;
    }

    public String getBasename ()
    {
        if ( this.segments.length == 0 )
        {
            return "";
        }

        return this.segments[this.segments.length - 1];
    }

    public String getDirname ()
    {
        if ( this.segments.length <= 1 )
        {
            return "";
        }
        else
        {
            return Arrays.stream ( this.segments, 0, this.segments.length - 1 /*exclusive*/ ).collect ( joiner () );
        }
    }

    @Override
    public String toString ()
    {
        return Arrays.stream ( this.segments ).collect ( joiner () );
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode ( this.segments );
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
        final PathName other = (PathName)obj;
        if ( !Arrays.equals ( this.segments, other.segments ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo ( final PathName o )
    {
        return toString ().compareTo ( o.toString () );
    }

    private Collector<CharSequence, ?, String> joiner ()
    {
        return Collectors.joining ( "/" );
    }

    public static PathName parse ( final String name )
    {
        final String[] parsed = name.split ( "/+" );

        final List<String> segs = new ArrayList<> ( parsed.length );
        for ( final String seg : parsed )
        {
            if ( seg.isEmpty () )
            {
                continue;
            }
            segs.add ( seg );
        }
        return new PathName ( segs );
    }
}
