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
package org.eclipse.packagedrone.repo.channel.transfer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.ByteStreams;

@FunctionalInterface
public interface ContentProvider
{
    public void provide ( OutputStream stream ) throws IOException;

    public static ContentProvider string ( final String value )
    {
        return stream -> stream.write ( value.getBytes ( StandardCharsets.UTF_8 ) );
    }

    public static ContentProvider data ( final byte[] value )
    {
        return stream -> stream.write ( value );
    }

    public static ContentProvider file ( final Path path )
    {
        return stream -> {
            try ( InputStream input = new BufferedInputStream ( Files.newInputStream ( path ) ) )
            {
                ByteStreams.copy ( input, stream );
            }
        };
    }
}
