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
package org.eclipse.packagedrone.storage.apm.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;

/**
 * An OutputStream which replaces the specified file when closing the stream
 * <p>
 * The basic idea of this OutputStream is that when the stream is being fully
 * written and closed, the backing file will simply be swapped out. This should
 * ensure that only a completely written file will be able to replace the
 * original file.
 * </p>
 * <p>
 * The {@link #commit()} method needs to be called before calling close,
 * otherwise the file will not be overwritten with the new content, but the new
 * content will be discarded.
 * </p>
 * <p>
 * This implementation redirects all output to a file beside the original file
 * and atomically replaces the target file in the {@link #close()} method if the
 * {@link #commit()} method was called at least once before.
 * </p>
 */
public class ReplaceOnCloseOutputStream extends OutputStream
{
    private final OutputStream out;

    private final Path targetName;

    private final Path tmp;

    private boolean commited;

    private boolean closed;

    public ReplaceOnCloseOutputStream ( final Path path ) throws IOException
    {
        this ( path, null );
    }

    public ReplaceOnCloseOutputStream ( final Path path, final Function<OutputStream, OutputStream> streamCustomizer ) throws IOException
    {
        this.targetName = path;

        // select target file name "original.dat.swp"

        this.tmp = path.resolveSibling ( path.getName ( path.getNameCount () - 1 ).toString () + ".swp" );

        // delete temp file ... ensure we can start fresh

        Files.deleteIfExists ( this.tmp );

        if ( streamCustomizer != null )
        {
            this.out = streamCustomizer.apply ( Files.newOutputStream ( this.tmp ) );
        }
        else
        {
            this.out = Files.newOutputStream ( this.tmp );
        }
    }

    @Override
    public void write ( final int b ) throws IOException
    {
        this.out.write ( b );
    }

    @Override
    public void write ( final byte[] b ) throws IOException
    {
        this.out.write ( b );
    }

    @Override
    public void write ( final byte[] b, final int off, final int len ) throws IOException
    {
        this.out.write ( b, off, len );
    }

    @Override
    public void flush () throws IOException
    {
        this.out.flush ();
    }

    public void commit ()
    {
        this.commited = true;
    }

    @Override
    public void close () throws IOException
    {
        if ( this.closed )
        {
            return;
        }

        this.closed = true;

        try
        {
            this.out.close ();
        }
        finally
        {
            try
            {
                if ( this.commited )
                {
                    Files.move ( this.tmp, this.targetName, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING );
                }
            }
            finally
            {
                Files.deleteIfExists ( this.tmp );
            }
        }
    }

}
