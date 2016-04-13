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
package org.eclipse.packagedrone.utils.rpm.tests;

import org.eclipse.packagedrone.utils.rpm.PathName;
import org.junit.Assert;
import org.junit.Test;

public class PathNameTest
{
    @Test
    public void test1 ()
    {
        assertPath ( "/", "", "" );
    }

    @Test
    public void test2 ()
    {
        assertPath ( "/foo", "", "foo" );
    }

    @Test
    public void test3 ()
    {
        assertPath ( "/foo/bar", "foo", "bar" );
    }

    @Test
    public void test4 ()
    {
        assertPath ( "/foo/bar/baz", "foo/bar", "baz" );
    }

    @Test
    public void test4a ()
    {
        assertPath ( "/foo//bar/baz", "foo/bar", "baz" );
    }

    @Test
    public void test4b ()
    {
        assertPath ( "/foo//bar/baz/", "foo/bar", "baz" );
    }

    @Test
    public void test4c ()
    {
        assertPath ( "foo//bar/baz/", "foo/bar", "baz" );
    }

    private void assertPath ( final String pathName, final String dirname, final String basename )
    {
        final PathName result = PathName.parse ( pathName );
        Assert.assertEquals ( dirname, result.getDirname () );
        Assert.assertEquals ( basename, result.getBasename () );
    }
}
