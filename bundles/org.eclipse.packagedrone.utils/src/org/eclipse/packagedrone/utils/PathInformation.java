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
package org.eclipse.packagedrone.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

public class PathInformation implements Iterator<String>
{
    private final String path;

    private final int length;

    private int current;

    public PathInformation ( final String path )
    {
        Objects.requireNonNull ( path );

        this.path = path;
        this.length = path.length ();
        this.current = 0;

        eatUp ();
    }

    @Override
    public boolean hasNext ()
    {
        return this.current < this.length;
    }

    @Override
    public String next ()
    {
        if ( !hasNext () )
        {
            throw new NoSuchElementException ();
        }

        final StringBuilder sb = new StringBuilder ();
        while ( this.current < this.length )
        {
            final char c = this.path.charAt ( this.current );
            if ( c == '/' )
            {
                eatUp ();
                break;
            }
            sb.append ( c );
            this.current++;
        }
        return sb.toString ();
    }

    private void eatUp ()
    {
        while ( this.current < this.length )
        {
            final char c = this.path.charAt ( this.current );
            if ( c != '/' )
            {
                break;
            }
            this.current++;
        }
    }

    public String getRemainder ()
    {
        return Arrays.stream ( getRawRemainder ().split ( "/+" ) ).collect ( Collectors.joining ( "/" ) );
    }

    public String getRawRemainder ()
    {
        if ( !hasNext () )
        {
            throw new NoSuchElementException ();
        }

        return this.path.substring ( this.current );
    }
}
