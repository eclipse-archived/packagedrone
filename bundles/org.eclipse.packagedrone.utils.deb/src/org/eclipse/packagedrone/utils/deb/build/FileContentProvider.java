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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileContentProvider implements ContentProvider
{

    private final File file;

    public FileContentProvider ( final File file )
    {
        this.file = file;
    }

    @Override
    public long getSize ()
    {
        return this.file.length ();
    }

    @Override
    public InputStream createInputStream () throws IOException
    {
        if ( this.file == null )
        {
            return null;
        }

        return new FileInputStream ( this.file );
    }

    @Override
    public boolean hasContent ()
    {
        return this.file != null;
    }

}
