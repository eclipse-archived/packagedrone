/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.tests;

import java.lang.reflect.Method;

import org.eclipse.packagedrone.web.controller.Controllers;
import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation;
import org.junit.Assert;
import org.junit.Test;

public class ControllersTest
{
    @Test
    public void testA () throws NoSuchMethodException, SecurityException
    {
        testMethod ( TestController.class, "mainA", new String[] { "/a" }, "GET" );
    }

    @Test
    public void testB () throws NoSuchMethodException, SecurityException
    {
        testMethod ( TestController.class, "mainB", new String[] { "/a/b" }, "GET" );
    }

    @Test
    public void testSub1A () throws NoSuchMethodException, SecurityException
    {
        testMethod ( SubTestController1.class, "mainA", new String[] { "/sub1" }, "GET" );
    }

    @Test
    public void testSub1B () throws NoSuchMethodException, SecurityException
    {
        testMethod ( SubTestController1.class, "mainB", new String[] { "/sub1/b" }, "GET" );
    }

    @Test
    public void testSub2C () throws NoSuchMethodException, SecurityException
    {
        testMethod ( SubTestController2.class, "mainC", new String[] { "/a/c" }, "GET" );
    }

    protected void testMethod ( final Class<?> clazz, final String name, final String[] expectedPaths, final String httpMethod ) throws NoSuchMethodException
    {
        final Method m = clazz.getMethod ( name );
        final RequestMappingInformation rmi = Controllers.fromMethod ( clazz, m );

        Assert.assertNotNull ( "No mapping information", rmi );

        for ( final String p : expectedPaths )
        {
            Assert.assertNotNull ( "No match for: " + p, rmi.matches ( p, httpMethod ) );
        }
    }
}
