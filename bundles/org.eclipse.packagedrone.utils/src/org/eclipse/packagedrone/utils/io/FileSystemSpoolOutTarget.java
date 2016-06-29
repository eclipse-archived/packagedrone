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
package org.eclipse.packagedrone.utils.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A spool out target based on the local file system
 */
public class FileSystemSpoolOutTarget implements SpoolOutTarget
{
    private final Path basePath;

    public FileSystemSpoolOutTarget ( final Path basePath )
    {
        this.basePath = basePath;
    }

    @Override
    public void spoolOut ( final String fileName, final String mimeType, final IOConsumer<OutputStream> streamConsumer ) throws IOException
    {
        final Path path = this.basePath.resolve ( fileName );
        Files.createDirectories ( path.getParent () );
        try ( OutputStream stream = new BufferedOutputStream ( Files.newOutputStream ( path ) ) )
        {
            streamConsumer.accept ( stream );
        }
    }

}
