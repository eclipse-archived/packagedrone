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

import java.util.NoSuchElementException;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

public class PathInformationTest
{
    @Test ( expected = NullPointerException.class )
    public void test1 ()
    {
        new PathInformation ( null );
    }

    @Test
    public void test2a ()
    {
        test ( "/foo/bar/baz", null, "foo", "bar", "baz" );
        test ( "/foo/bar/baz", "bar/baz", "foo" );

        test ( "/", null );
        test ( "/foo", null, "foo" );
        test ( "/foo", "foo" );
    }

    @Test
    public void test2b ()
    {
        test ( "foo/bar/baz", null, "foo", "bar", "baz" );
        test ( "foo/bar/baz", "bar/baz", "foo" );

        test ( "", null );
        test ( "foo", null, "foo" );
        test ( "foo", "foo" );
    }

    @Test
    public void test2c ()
    {
        test ( "//foo//bar//baz", null, "foo", "bar", "baz" );
        test ( "//foo//bar//baz", "bar/baz", "foo" );

        test ( "//foo//bar//baz//", null, "foo", "bar", "baz" );
        test ( "//foo//bar//baz//", "bar/baz", "foo" );

        test ( "//", null );
        test ( "//foo", null, "foo" );
        test ( "//foo", "foo" );

        test ( "////", null );
        test ( "//foo//", null, "foo" );
        test ( "//foo//", "foo" );
    }

    @Test ( expected = NoSuchElementException.class )
    public void test3a ()
    {
        final PathInformation pi = new PathInformation ( "/foo/bar" );
        pi.next ();
        pi.next ();
        pi.next (); // fail
    }

    @Test
    public void test4a ()
    {
        testRaw ( "//foo//bar//baz", null, "foo", "bar", "baz" );
        testRaw ( "//foo//bar//baz", "bar//baz", "foo" );

        testRaw ( "//foo//bar//baz//", null, "foo", "bar", "baz" );
        testRaw ( "//foo//bar//baz//", "foo//bar//baz//" );
        testRaw ( "//foo//bar//baz//", "bar//baz//", "foo" );

        testRaw ( "//", null );
        testRaw ( "//foo", null, "foo" );
        testRaw ( "//foo", "foo" );

        testRaw ( "////", null );
        testRaw ( "//foo//", null, "foo" );
        testRaw ( "//foo//", "foo//" );
    }

    @Test ( expected = NoSuchElementException.class )
    public void test3b ()
    {
        final PathInformation pi = new PathInformation ( "/" );
        pi.next (); // fail
    }

    private void test ( final String path, final String remainder, final String... tokens )
    {
        testRem ( path, PathInformation::getRemainder, remainder, tokens );
    }

    private void testRaw ( final String path, final String remainder, final String... tokens )
    {
        testRem ( path, PathInformation::getRawRemainder, remainder, tokens );
    }

    private void testRem ( final String path, final Function<PathInformation, String> remainderSupplier, final String remainder, final String... tokens )
    {
        final PathInformation pi = new PathInformation ( path );

        for ( final String token : tokens )
        {
            Assert.assertEquals ( token, pi.next () );
        }

        if ( remainder != null )
        {
            Assert.assertEquals ( remainder, remainderSupplier.apply ( pi ) );
        }
        else
        {
            try
            {
                final String rem = remainderSupplier.apply ( pi );
                Assert.fail ( "Expected NoSuchElementException. Got: " + rem );
            }
            catch ( final NoSuchElementException e )
            {
            }
        }
    }
}
