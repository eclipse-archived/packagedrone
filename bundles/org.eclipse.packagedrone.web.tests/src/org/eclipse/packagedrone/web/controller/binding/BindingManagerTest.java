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
package org.eclipse.packagedrone.web.controller.binding;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class BindingManagerTest
{
    public static class Test1
    {
        private boolean flag = true;

        public void setFlag ( final boolean flag )
        {
            this.flag = flag;
        }

        public boolean isFlag ()
        {
            return this.flag;
        }
    }

    @Test
    public void test1 () throws Exception
    {
        testFlag ( "on", true );
    }

    @Test
    public void test2 () throws Exception
    {
        testFlag ( null, false );
    }

    @Test
    public void test3 () throws Exception
    {
        testFlag ( "off", false );
    }

    @Test
    public void test4 () throws Exception
    {
        testFlag ( "true", true );
    }

    @Test
    public void test5 () throws Exception
    {
        testFlag ( "false", false );
    }

    private static void testFlag ( final String input, final boolean expected ) throws Exception
    {
        final Map<String, Object> data = new HashMap<> ();
        data.put ( "flag", input );

        final BindingManager bm = BindingManager.create ( data );

        final Test1 t1 = new Test1 ();
        bm.bindProperties ( t1 );

        Assert.assertEquals ( expected, t1.isFlag () );
    }
}
