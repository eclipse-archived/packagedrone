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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class UnsignedTest
{
    @Test
    public void test1 () throws IOException
    {
        final long value = 0xFFFFFFFFL;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        final DataOutput dos = new DataOutputStream ( bos );

        dos.writeInt ( (int)value );

        Assert.assertEquals ( 4, bos.size () );

        final DataInputStream dis = new DataInputStream ( new ByteArrayInputStream ( bos.toByteArray () ) );

        Assert.assertEquals ( value, dis.readInt () & 0xFFFFFFFFL );
    }
}
