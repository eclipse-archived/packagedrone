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

import java.util.List;
import java.util.Objects;

public class PaginationResult<T>
{
    private final List<T> data;

    private final boolean previous;

    private final boolean next;

    private final int pageNumber;

    private final int pageSize;

    public PaginationResult ( final List<T> data, final boolean previous, final boolean next, final int pageNumber, final int pageSize )
    {
        this.data = Objects.requireNonNull ( data, "'data' must not be null" );
        this.previous = previous;
        this.next = next;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public List<T> getData ()
    {
        return this.data;
    }

    public boolean isPrevious ()
    {
        return this.previous;
    }

    public boolean isNext ()
    {
        return this.next;
    }

    public int getPageNumber ()
    {
        return this.pageNumber;
    }

    public int getPageSize ()
    {
        return this.pageSize;
    }

    public int getPreviousPage ()
    {
        return Math.max ( 0, this.pageNumber - 1 );
    }

    public int getNextPage ()
    {
        return this.pageNumber + 1;
    }
}
