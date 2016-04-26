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
package org.eclipse.packagedrone.utils.converter;

import org.junit.Assert;
import org.junit.Test;

public class ConverterTest
{
    private final ConverterManager manager;

    public ConverterTest ()
    {
        this.manager = ConverterManager.create ();
    }

    @Test
    public void testString1 ()
    {
        testDefault ( String.class, "foo", "foo" );
    }

    @Test
    public void testString2 ()
    {
        testDefault ( String[].class, new String[] { "foo" }, "foo" );
    }

    @Test
    public void testString3 ()
    {
        testDefault ( String[].class, new String[] { "foo bar" }, "foo bar" );
    }

    @Test
    public void testString4 ()
    {
        testDefault ( String[].class, new String[] { "foo", "bar" }, new String[] { "foo", "bar" } );
    }

    @Test
    public void testLong1 ()
    {
        testDefault ( Long.class, 1L, 1L );
    }

    @Test
    public void testLong2 ()
    {
        testDefault ( Long.class, 1L, "1" );
    }

    @Test
    public void testBoolean1 ()
    {
        testDefault ( boolean.class, true, "on" );
    }

    @Test
    public void testBoolean2 ()
    {
        testDefault ( boolean.class, true, "true" );
    }

    @Test
    public void testBoolean3 ()
    {
        testDefault ( boolean.class, false, "false" );
    }

    @Test
    public void testBoolean4 ()
    {
        testDefault ( boolean.class, false, null );
    }

    private void testDefault ( final Class<?> clazz, final Object expected, final Object value )
    {
        final Object result = this.manager.convertTo ( value, clazz );
        if ( expected.getClass ().isArray () )
        {
            Assert.assertArrayEquals ( (Object[])expected, (Object[])result );
        }
        else
        {
            Assert.assertEquals ( expected, result );
        }
    }
}
