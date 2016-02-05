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
package org.eclipse.packagedrone.web.tests;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.web.controller.binding.BindingManager;
import org.eclipse.packagedrone.web.controller.binding.BindingManager.Call;
import org.eclipse.packagedrone.web.controller.binding.MapBinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BindingTest
{
    private BindingManager mgr;

    @Before
    public void setup ()
    {
        final Map<String, Object> model = new HashMap<> ();
        model.put ( "key1", "bar" );
        model.put ( "key2", 1 );

        final MapBinder mb = new MapBinder ( model );
        this.mgr = new BindingManager ();

        this.mgr.addBinder ( mb );
    }

    @Test
    public void testPlainMethod () throws Exception
    {
        testMethod ( this.mgr, this, "testMethod1", "bar" );
    }

    @Test
    public void testGenericMethod () throws Exception
    {
        testMethod ( this.mgr, this, "testMethod2", "bar" ); // will bind to Object.class
    }

    @Test
    public void testGeneric1Class1 () throws Exception
    {
        final GenericController1<String> controller = new GenericController1<String> () {};
        testMethod ( this.mgr, controller, "testMethod1", "bar" );
    }

    @Test
    public void testGeneric1Class2 () throws Exception
    {
        final GenericController1<Integer> controller = new GenericController1<Integer> () {};
        testMethod ( this.mgr, controller, "testMethod1", 1 );
    }

    @Test
    public void testGeneric2Class1 () throws Exception
    {
        final GenericController1<String> controller = new GenericController2<String> () {};
        testMethod ( this.mgr, controller, "testMethod1", "bar" );
    }

    @Test
    public void testGeneric2Class2 () throws Exception
    {
        final GenericController1<Integer> controller = new GenericController2<Integer> () {};
        testMethod ( this.mgr, controller, "testMethod1", 1 );
    }

    @Test
    public void testParamStringClass () throws Exception
    {
        final GenericController1<?> controller = new ParametrizedControllerString ();
        testMethod ( this.mgr, controller, "testMethod1", "bar" );
    }

    @Test
    public void testParamIntegerClass () throws Exception
    {
        final GenericController1<?> controller = new ParametrizedControllerInteger ();
        testMethod ( this.mgr, controller, "testMethod1", 1 );
    }

    private void testMethod ( final BindingManager mgr, final Object controller, final String methodName, final Object expectedResult ) throws Exception
    {
        final Method m = getMethod ( controller.getClass (), methodName );

        System.out.println ( m );

        final Call call = mgr.bind ( m, controller );
        final Object result = call.invoke ();
        System.out.format ( "\tresult -> %s (%s)", result, result.getClass () );

        System.out.println ();

        Assert.assertEquals ( expectedResult, result );
    }

    private Method getMethod ( final Class<?> clazz, final String methodName )
    {
        for ( final Method m : clazz.getMethods () )
        {
            if ( m.getName ().equals ( methodName ) )
            {
                return m;
            }
        }
        return null;
    }

    public String testMethod1 ( final String value )
    {
        return value;
    }

    public <T> T testMethod2 ( final T value )
    {
        return value;
    }
}
