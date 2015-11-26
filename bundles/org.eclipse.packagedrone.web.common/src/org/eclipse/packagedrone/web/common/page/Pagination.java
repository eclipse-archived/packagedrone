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
package org.eclipse.packagedrone.web.common.page;

import java.util.Collections;
import java.util.List;

public final class Pagination
{
    private Pagination ()
    {
    }

    @FunctionalInterface
    public interface Provider<T>
    {
        public List<T> list ( int startIndex, int length );
    }

    public static <T> PaginationResult<T> paginate ( Integer startPage, final int pageSize, final Provider<T> provider )
    {
        if ( pageSize <= 0 )
        {
            throw new IllegalArgumentException ( String.format ( "Page size must be greater than zero" ) );
        }

        if ( startPage == null || startPage < 0 )
        {
            startPage = 0;
        }

        final int start = startPage * pageSize;

        List<T> data = provider.list ( start, pageSize + 1 ); // request one more than necessary to peek for a next page

        // check for trailing entries
        final boolean hasNext = data.size () > pageSize;

        // now remove the trailing entries
        if ( data.size () > pageSize )
        {
            data = data.subList ( 0, pageSize );
        }

        return new PaginationResult<> ( data, start > 0, hasNext, startPage, pageSize );
    }

    public static <T> PaginationResult<T> paginate ( final Integer startPage, final int pageSize, final List<T> fullDataSet )
    {
        return paginate ( startPage, pageSize, ( start, length ) -> {

            final int len = fullDataSet.size ();
            if ( start > len )
            {
                return Collections.emptyList ();
            }

            return fullDataSet.subList ( start, Math.min ( start + length, len ) );
        } );
    }
}
