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
package org.eclipse.packagedrone.web.tests.tags;

import java.util.Arrays;

import org.eclipse.packagedrone.web.tags.JsonFunctions;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonParser;

public class JsonFunctionsTest
{
    @Test
    public void test1Null ()
    {
        testToJson ( null, "[]" );
    }

    @Test
    public void test2Empty ()
    {
        testToJson ( new Object[] {}, "[]" );
    }

    @Test
    public void test3Plain ()
    {
        testToJson ( "foo", "[\"foo\"]" );
    }

    @Test
    public void test4SingleArrayObject ()
    {
        testToJson ( new Object[] { "foo" }, "[\"foo\"]" );
    }

    @Test
    public void test5SingleArrayString ()
    {
        testToJson ( new String[] { "foo" }, "[\"foo\"]" );
    }

    @Test
    public void test6MultiStringArray ()
    {
        testToJson ( new String[] { "foo", "bar" }, "[\"foo\",\"bar\"]" );
    }

    @Test
    public void test7MultiList ()
    {
        testToJson ( Arrays.asList ( "foo", "bar" ), "[\"foo\",\"bar\"]" );
    }

    @Test
    public void test8MultiStream ()
    {
        testToJson ( Arrays.asList ( "foo", "bar" ).stream (), "[\"foo\",\"bar\"]" );
    }

    @Test
    public void test8MultiIterator ()
    {
        testToJson ( Arrays.asList ( "foo", "bar" ).iterator (), "[\"foo\",\"bar\"]" );
    }

    @Test
    public void test8MultiIterable ()
    {
        final Iterable<?> iter = Arrays.asList ( "foo", "bar" );
        testToJson ( iter, "[\"foo\",\"bar\"]" );
    }

    @Test
    public void test9Numbers ()
    {
        testToJson ( new Object[] { 0L, 1L, 2L }, "[\"0\",\"1\",\"2\"]" );
    }

    @Test
    public void test10Boolean ()
    {
        testToJson ( new Object[] { true, false, true }, "[\"true\",\"false\",\"true\"]" );
    }

    private void testToJson ( final Object value, final String expected )
    {
        final String result = JsonFunctions.toJson ( value );

        try
        {
            new JsonParser ().parse ( result );
        }
        catch ( final Exception e )
        {
            Assert.fail ( "Produced invalid JSON" );
        }

        Assert.assertEquals ( expected, result );
    }
}
