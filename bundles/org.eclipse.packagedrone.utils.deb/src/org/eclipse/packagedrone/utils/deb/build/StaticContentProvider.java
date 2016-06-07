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
package org.eclipse.packagedrone.utils.deb.build;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StaticContentProvider implements ContentProvider
{
    private final byte[] data;

    public StaticContentProvider ( final byte[] data )
    {
        this.data = data;
    }

    public StaticContentProvider ( final String data )
    {
        this ( data.getBytes ( DebianPackageWriter.CHARSET ) );
    }

    @Override
    public long getSize ()
    {
        return this.data.length;
    }

    @Override
    public InputStream createInputStream () throws IOException
    {
        if ( this.data == null )
        {
            return null;
        }

        return new ByteArrayInputStream ( this.data );
    }

    @Override
    public boolean hasContent ()
    {
        return this.data != null;
    }

}
