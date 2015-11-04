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
package org.eclipse.packagedrone.utils.deb.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.utils.deb.ControlFileParser;
import org.eclipse.packagedrone.utils.deb.ControlFileWriter;
import org.eclipse.packagedrone.utils.deb.FieldFormatter;
import org.eclipse.packagedrone.utils.deb.Packages;
import org.eclipse.packagedrone.utils.deb.ParserException;
import org.junit.Test;

import com.google.common.io.CharStreams;

public class PackagesTest
{

    @Test
    public void test1FieldFormatters () throws IOException
    {
        testFieldFormatterValue ( FieldFormatter.SINGLE, "foo", "foo" );
        testFieldFormatter ( FieldFormatter.SINGLE, "Foo", "bar", "Foo: bar" );

        testFieldFormatterValue ( FieldFormatter.MULTI, "foo", "foo" );
        testFieldFormatter ( FieldFormatter.MULTI, "Foo", "bar", "Foo: bar" );

        testFieldFormatterValue ( FieldFormatter.MULTI, "foo\nbar", "foo\n bar" );
        testFieldFormatterValue ( FieldFormatter.MULTI, "foo\n\nbar", "foo\n .\n bar" );
        testFieldFormatterValue ( FieldFormatter.MULTI, "\nfoo\n\nbar\n\n", "\n foo\n .\n bar" );
    }

    @Test
    public void test1FieldFormattersCornerCases () throws IOException
    {
        testFieldFormatterValue ( FieldFormatter.SINGLE, "foo\nbar", "foobar" );
        testFieldFormatter ( FieldFormatter.SINGLE, "Foo", "bar\nbar", "Foo: barbar" );

        testFieldFormatterValue ( FieldFormatter.SINGLE, "", "" );
        testFieldFormatter ( FieldFormatter.SINGLE, "Foo", "", "Foo:" );

        testFieldFormatterValue ( FieldFormatter.MULTI, "", "" );
        testFieldFormatter ( FieldFormatter.MULTI, "Foo", "", "Foo:" );

        testFieldFormatterValue ( FieldFormatter.MULTI, "\n", "" );
        testFieldFormatter ( FieldFormatter.MULTI, "Foo", "\n", "Foo:" );

        testFieldFormatterValue ( FieldFormatter.MULTI, "\n\n", "" );
        testFieldFormatter ( FieldFormatter.MULTI, "Foo", "\n", "Foo:" );
    }

    private void testFieldFormatter ( final FieldFormatter formatter, final String key, final String input, final String expected ) throws IOException
    {
        final StringBuilder sb = new StringBuilder ();
        formatter.append ( key, input, sb );
        assertEquals ( expected, formatter.format ( key, input ) );
    }

    private void testFieldFormatterValue ( final FieldFormatter formatter, final String input, final String expected ) throws IOException
    {
        final StringBuilder sb = new StringBuilder ();
        formatter.appendValue ( input, sb );

        /*
        System.out.println ( "Expected ->" );
        System.out.println ( expected );
        System.out.println ( "Actual ->" );
        System.out.println ( sb.toString () );
        */

        assertEquals ( expected, formatter.formatValue ( input ) );
    }

    @Test
    public void test2 () throws IOException, ParserException
    {
        LinkedHashMap<String, String> control;
        try ( InputStream is = PackagesTest.class.getResourceAsStream ( "data/test1" ) )
        {
            control = ControlFileParser.parse ( is );
        }

        final String md5 = Packages.makeDescriptionMd5 ( control.get ( "Description" ) );

        assertEquals ( "38d96b653196d5ef8c667efe23411a81", md5 );
    }

    @Test
    public void test3 () throws IOException, ParserException
    {
        LinkedHashMap<String, String> control;
        try ( InputStream is = PackagesTest.class.getResourceAsStream ( "data/test2" ) )
        {
            control = ControlFileParser.parse ( is );
        }

        assertEquals ( "org.eclipse.scada.base.p2-incubation", control.get ( "Package" ) );
        assertEquals ( "1100", control.get ( "Installed-Size" ) );
        assertEquals ( "Eclipse SCADA P2 Repository - org.eclipse.scada.base.p2-incubation", control.get ( "Description" ) );
        assertEquals ( "\n/file1 1234\n/file2 1234", control.get ( "Conffiles" ) );
    }

    @Test
    public void test4 () throws IOException, ParserException
    {
        encodeDecodeTest ( "data/test1" );
        encodeDecodeTest ( "data/test2" );
    }

    private void encodeDecodeTest ( final String resourceName ) throws IOException, ParserException
    {
        LinkedHashMap<String, String> control;
        try ( InputStream is = PackagesTest.class.getResourceAsStream ( resourceName ) )
        {
            control = ControlFileParser.parse ( is );
        }

        final StringBuilder sb = new StringBuilder ();
        final Map<String, FieldFormatter> map = new HashMap<> ();
        map.put ( "Description", FieldFormatter.MULTI );
        map.put ( "Conffiles", FieldFormatter.MULTI );
        new ControlFileWriter ( sb, map ).writeEntries ( control );

        String data;
        try ( InputStream is = PackagesTest.class.getResourceAsStream ( resourceName ) )
        {
            data = CharStreams.toString ( new InputStreamReader ( is, StandardCharsets.UTF_8 ) );
        }

        System.out.println ( sb.toString () );

        assertEquals ( data, sb.toString () );
    }

    @Test
    public void testMultiFile1 () throws IOException, ParserException
    {
        List<Map<String, String>> result;
        try ( InputStream is = PackagesTest.class.getResourceAsStream ( "data/test3" ) )
        {
            result = Packages.parseStatusFile ( is );
        }

        assertEquals ( 2, result.size () );
    }
}
