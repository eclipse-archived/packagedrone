/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.deb.tests;

import java.io.StringWriter;
import java.util.Optional;

import org.eclipse.packagedrone.utils.deb.ControlFileWriter;
import org.eclipse.packagedrone.utils.deb.FieldFormatter;
import org.junit.Assert;
import org.junit.Test;

public class ControlFileWriterTest
{
    private static class ControlFieldDefinition
    {
        private final String name;

        private final FieldFormatter formatter;

        ControlFieldDefinition ( final String name, final FieldFormatter formatter )
        {
            this.name = name;
            this.formatter = formatter;
        }
    }

    private static final ControlFieldDefinition defPackage = new ControlFieldDefinition ( "Package", FieldFormatter.SINGLE );

    private static final ControlFieldDefinition defDescription = new ControlFieldDefinition ( "Description", FieldFormatter.MULTI );

    @Test
    public void test1 () throws Exception
    {
        testField ( defPackage, "libc", "Package: libc\n" );
        testField ( defDescription, "Hello World", "Description: Hello World\n" );
        testField ( defDescription, "Foo Bar\nHello World", "Description: Foo Bar\n Hello World\n" );
        testField ( defDescription, "Foo Bar\nHello World\nline2", "Description: Foo Bar\n Hello World\n line2\n" );
        testField ( defDescription, "Foo Bar\nHello World\n\nline2", "Description: Foo Bar\n Hello World\n .\n line2\n" );
    }

    private void testField ( final ControlFieldDefinition field, final String value, final String expectedResult ) throws Exception
    {
        final StringWriter sw = new StringWriter ();
        final ControlFileWriter writer = new ControlFileWriter ( sw );
        writer.writeEntry ( field.name, value, Optional.ofNullable ( field.formatter ) );
        sw.close ();
        final String result = sw.toString ();
        System.out.println ( "Actual: '" + result + "'" );
        Assert.assertEquals ( "Field encoding", expectedResult, result );
    }
}
