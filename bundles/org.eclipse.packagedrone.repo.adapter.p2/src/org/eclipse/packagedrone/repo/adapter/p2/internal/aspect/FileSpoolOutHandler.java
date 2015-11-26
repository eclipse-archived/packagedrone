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
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.packagedrone.utils.io.IOConsumer;

public class FileSpoolOutHandler implements SpoolOutHandler
{
    private final File base;

    public FileSpoolOutHandler ( final File base )
    {
        this.base = base;
    }

    @Override
    public void spoolOut ( final String id, final String name, final String mimeType, final IOConsumer<OutputStream> stream ) throws IOException
    {
        final File file = new File ( this.base, name );
        file.getParentFile ().mkdirs ();

        try ( OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) ) )
        {
            stream.accept ( null );
        }
    }

}
