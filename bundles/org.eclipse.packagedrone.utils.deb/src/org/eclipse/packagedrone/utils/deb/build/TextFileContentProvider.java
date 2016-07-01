/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S1943
 *******************************************************************************/
package org.eclipse.packagedrone.utils.deb.build;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public class TextFileContentProvider implements ContentProvider
{
    private final byte[] data;

    public TextFileContentProvider ( final File file ) throws FileNotFoundException, IOException
    {
        try ( FileInputStream fileInputStream = new FileInputStream ( file ) )
        {
            InputStreamReader reader = new InputStreamReader ( fileInputStream, StandardCharsets.UTF_8 );
            if ( file != null )
            {
                String data = IOUtils.toString ( reader );
                if ( needFix () )
                {
                    data = fix ( data );
                }
                this.data = data.getBytes ( StandardCharsets.UTF_8 );
            }
            else
            {
                this.data = null;
            }
        }

    }

    private static boolean needFix ()
    {
        return !"\n".equals ( System.lineSeparator () );
    }

    private static String fix ( final String data )
    {
        return data.replace ( "\r\n", "\n" );
    }

    @Override
    public long getSize ()
    {
        return this.data == null ? 0 : this.data.length;
    }

    @Override
    public InputStream createInputStream () throws IOException
    {
        if ( this.data != null )
        {
            return new ByteArrayInputStream ( this.data );
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean hasContent ()
    {
        return this.data != null;
    }

}
