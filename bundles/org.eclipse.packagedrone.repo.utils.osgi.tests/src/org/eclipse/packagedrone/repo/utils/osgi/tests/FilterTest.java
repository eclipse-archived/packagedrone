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
package org.eclipse.packagedrone.repo.utils.osgi.tests;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.packagedrone.utils.Filters;
import org.eclipse.packagedrone.utils.Filters.Multi;
import org.eclipse.packagedrone.utils.Filters.Pair;
import org.junit.Assert;
import org.junit.Test;

public class FilterTest
{
    @Test
    public void test1 ()
    {
        Assert.assertEquals ( "(|(a=b)(c=d))", Filters.or ( pairs ( "a", "b", "c", "d" ) ) );
        Assert.assertEquals ( "(a=b)", Filters.or ( pairs ( "a", "b" ) ) );
        Assert.assertEquals ( "", Filters.or ( pairs () ) );
    }

    @Test
    public void test2 ()
    {
        final Multi m = new Filters.Multi ( "&" );
        final Multi m1 = new Filters.Multi ( "|" );
        final Multi m2 = new Filters.Multi ( "|" );

        m.addNode ( m1 );
        m.addNode ( m2 );

        m2.addNode ( new Pair ( "foo", "bar" ) );

        Assert.assertEquals ( "(foo=bar)", m.toString () );
    }

    @Test
    public void test3 ()
    {
        final Multi m = new Filters.Multi ( "&" );
        final Multi m1 = new Filters.Multi ( "|" );
        final Multi m2 = new Filters.Multi ( "|" );

        m.addNode ( m1 );
        m.addNode ( m2 );

        m1.addNode ( new Pair ( "foo", "bar" ) );
        m2.addNode ( new Pair ( "foo2", "bar2" ) );

        Assert.assertEquals ( "(&(foo=bar)(foo2=bar2))", m.toString () );
    }

    @Test
    public void test4 ()
    {
        final Multi m = new Filters.Multi ( "&" );
        final Multi m1 = new Filters.Multi ( "|" );
        final Multi m2 = new Filters.Multi ( "|" );

        m.addNode ( m1 );
        m.addNode ( m2 );

        Assert.assertEquals ( "", m.toString () );
    }

    @Test
    public void test5 ()
    {
        final Multi m = new Filters.Multi ( "&" );
        final Multi m1 = new Filters.Multi ( "|" );
        final Multi m2 = new Filters.Multi ( "|" );

        m.addNode ( m1 );
        m.addNode ( m2 );

        m1.addNode ( new Pair ( "foo", "bar" ) );
        m1.addNode ( new Pair ( "answer", "42" ) );
        m2.addNode ( new Pair ( "foo2", "bar2" ) );
        m2.addNode ( new Pair ( "foz2", "baz2" ) );

        Assert.assertEquals ( "(&(|(foo=bar)(answer=42))(|(foo2=bar2)(foz2=baz2)))", m.toString () );
    }

    @Test
    public void test6 ()
    {
        final Multi m = new Filters.Multi ( "&" );
        final Multi m1 = new Filters.Multi ( "|" );
        final Multi m2 = new Filters.Multi ( "|" );

        m.addNode ( m1 );
        m.addNode ( m2 );

        m1.addNode ( new Pair ( "foo", "bar" ) );
        m1.addNode ( new Pair ( "answer", "42" ) );

        Assert.assertEquals ( "(|(foo=bar)(answer=42))", m.toString () );
    }

    private List<Pair> pairs ( final String... tokens )
    {
        int pos = 0;

        final List<Pair> result = new LinkedList<> ();

        while ( pos + 1 < tokens.length )
        {
            result.add ( new Pair ( tokens[pos], tokens[pos + 1] ) );
            pos += 2;
        }

        return result;
    }
}
