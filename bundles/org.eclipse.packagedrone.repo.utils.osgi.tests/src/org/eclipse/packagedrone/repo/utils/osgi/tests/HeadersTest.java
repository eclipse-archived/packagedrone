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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import org.eclipse.packagedrone.utils.AttributedValue;
import org.eclipse.packagedrone.utils.Headers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HeadersTest
{
    private Manifest mf1;

    private Manifest mf2;

    @Before
    public void setup () throws IOException
    {
        this.mf1 = new Manifest ( HeadersTest.class.getResourceAsStream ( "mf1.txt" ) );
        this.mf2 = new Manifest ( HeadersTest.class.getResourceAsStream ( "mf2.txt" ) );
    }

    @Test
    public void testSeg1 ()
    {
        final String v = this.mf1.getMainAttributes ().getValue ( "Export-Package" );

        final String[] segs = Headers.segments ( v );

        Assert.assertEquals ( 3, segs.length );
        Assert.assertEquals ( "de.dentrassi.pm.aspect.common.osgi", segs[0] );
        Assert.assertEquals ( "version=\"1.0.0\"", segs[1] );
        Assert.assertEquals ( "uses:=\"org.w3c.dom,  de.dentrassi.pm.aspect,  de.dentrassi.pm.osgi,  de.dentrassi.pm.aspect.extract,  de.dentrassi.pm.aspect.virtual\"", segs[2] );
    }

    @Test
    public void test1 ()
    {
        final AttributedValue av = Headers.parse ( "test" );
        Assert.assertEquals ( "test", av.getValue () );
        Assert.assertTrue ( av.getAttributes ().isEmpty () );
    }

    @Test
    public void test2 ()
    {
        final AttributedValue av = Headers.parse ( "test; singleton:=true" );
        Assert.assertEquals ( "test", av.getValue () );
        Assert.assertEquals ( 1, av.getAttributes ().size () );
        assertAttribute ( av.getAttributes (), "singleton", "true" );
    }

    @Test
    public void test3 ()
    {
        final AttributedValue av = Headers.parse ( "lazy; exclude:=\"org.foo, org.bar\"" );
        Assert.assertEquals ( "lazy", av.getValue () );
        Assert.assertEquals ( 1, av.getAttributes ().size () );
        assertAttribute ( av.getAttributes (), "exclude", "org.foo, org.bar" );
    }

    @Test
    public void test4 ()
    {
        final AttributedValue av = Headers.parse ( "org.junit;bundle-version=\"4.11.0\"" );
        Assert.assertEquals ( "org.junit", av.getValue () );
        Assert.assertEquals ( 1, av.getAttributes ().size () );
        assertAttribute ( av.getAttributes (), "bundle-version", "4.11.0" );
    }

    @Test
    public void test5 ()
    {
        final String v = this.mf1.getMainAttributes ().getValue ( "Export-Package" );

        final AttributedValue av = Headers.parse ( v );
        Assert.assertEquals ( "de.dentrassi.pm.aspect.common.osgi", av.getValue () );
        Assert.assertEquals ( 2, av.getAttributes ().size () );
        assertAttribute ( av.getAttributes (), "version", "1.0.0" );
        assertAttribute ( av.getAttributes (), "uses", "org.w3c.dom,  de.dentrassi.pm.aspect,  de.dentrassi.pm.osgi,  de.dentrassi.pm.aspect.extract,  de.dentrassi.pm.aspect.virtual" );
    }

    @Test
    public void testFull1 ()
    {
        final String v = this.mf1.getMainAttributes ().getValue ( "Export-Package" );

        final List<AttributedValue> avs = Headers.parseList ( v );

        Assert.assertEquals ( 1, avs.size () );

        final AttributedValue av = avs.get ( 0 );

        Assert.assertEquals ( "de.dentrassi.pm.aspect.common.osgi", av.getValue () );
        Assert.assertEquals ( 2, av.getAttributes ().size () );
        assertAttribute ( av.getAttributes (), "version", "1.0.0" );
        assertAttribute ( av.getAttributes (), "uses", "org.w3c.dom,  de.dentrassi.pm.aspect,  de.dentrassi.pm.osgi,  de.dentrassi.pm.aspect.extract,  de.dentrassi.pm.aspect.virtual" );
    }

    @Test
    public void testFull2 ()
    {
        final String v1 = this.mf2.getMainAttributes ().getValue ( "Export-Package" );
        final List<AttributedValue> avs1 = Headers.parseList ( v1 );
        Assert.assertEquals ( 2, avs1.size () );

        final AttributedValue av1 = avs1.get ( 0 );
        final AttributedValue av2 = avs1.get ( 1 );

        Assert.assertEquals ( "de.dentrassi.pm.storage.service", av1.getValue () );
        Assert.assertEquals ( 2, av1.getAttributes ().size () );
        assertAttribute ( av1.getAttributes (), "version", "1.0.0" );
        assertAttribute ( av1.getAttributes (), "uses", "de.dentrassi.pm.storage" );

        Assert.assertEquals ( "de.dentrassi.pm.storage.service.util", av2.getValue () );
        Assert.assertEquals ( 2, av2.getAttributes ().size () );
        assertAttribute ( av2.getAttributes (), "version", "1.0.0" );
        assertAttribute ( av2.getAttributes (), "uses", "de.dentrassi.pm.storage.service,javax.servlet.http" );

        final String v2 = this.mf2.getMainAttributes ().getValue ( "Import-Package" );
        final List<AttributedValue> avs2 = Headers.parseList ( v2 );
        Assert.assertEquals ( 15, avs2.size () );

        final Map<String, AttributedValue> map = new HashMap<> ();
        for ( final AttributedValue av : avs2 )
        {
            map.put ( av.getValue (), av );
        }

        assertValue ( map, "com.google.common.io", "version", "18.0.0" );
        assertValue ( map, "javax.servlet", "version", "3.1.0" );
    }

    private void assertValue ( final Map<String, AttributedValue> map, final String key, final String attr, final String expectedValue )
    {
        final AttributedValue av = map.get ( key );
        Assert.assertNotNull ( av );
        final String value = av.getAttributes ().get ( attr );
        Assert.assertEquals ( expectedValue, value );
    }

    private static void assertAttribute ( final Map<String, String> attributes, final String key, final String expectedValue )
    {
        final String value = attributes.get ( key );
        Assert.assertNotNull ( value );
        Assert.assertEquals ( expectedValue, value );
    }

}
