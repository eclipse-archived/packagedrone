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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.packagedrone.web.common.page.Pagination;
import org.eclipse.packagedrone.web.common.page.PaginationResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PageTest
{
    private List<Integer> dataSet1;

    @Before
    public void setup ()
    {
        this.dataSet1 = new ArrayList<> ( 100 );
        for ( int i = 0; i < 100; i++ )
        {
            this.dataSet1.add ( i );
        }
    }

    @Test
    public void testStart ()
    {
        assertResult ( Pagination.paginate ( null, 25, this.dataSet1 ), false, true, 0, 24 );
        assertResult ( Pagination.paginate ( 0, 25, this.dataSet1 ), false, true, 0, 24 );
    }

    @Test
    public void testMiddle ()
    {
        assertResult ( Pagination.paginate ( 1, 25, this.dataSet1 ), true, true, 25, 49 );
        assertResult ( Pagination.paginate ( 2, 25, this.dataSet1 ), true, true, 50, 74 );
    }

    @Test
    public void testEnd ()
    {
        assertResult ( Pagination.paginate ( 3, 25, this.dataSet1 ), true, false, 75, 99 );
    }

    private void assertResult ( final PaginationResult<Integer> paginate, final boolean prev, final boolean next, final int startValue, final int endValue )
    {
        Assert.assertEquals ( prev, paginate.isPrevious () );
        Assert.assertEquals ( next, paginate.isNext () );

        final List<Integer> expected = new ArrayList<> ();
        for ( int i = startValue; i <= endValue; i++ )
        {
            expected.add ( i );
        }

        Assert.assertEquals ( expected, paginate.getData () );
    }
}
