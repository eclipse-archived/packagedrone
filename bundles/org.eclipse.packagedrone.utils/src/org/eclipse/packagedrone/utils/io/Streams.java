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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Helper class working with {@link InputStream}s and {@link OutputStream}s
 */
public final class Streams
{
    private static final int COPY_BUFFER_SIZE = 8 * 1024;

    private static final InputStream CLOSED_INPUT = new InputStream () {

        @Override
        public int read () throws IOException
        {
            return -1;
        }
    };

    private static final OutputStream CLOSED_OUTPUT = new OutputStream () {

        @Override
        public void write ( final int b ) throws IOException
        {
            throw new IOException ( "Failed to write: stream is closed" );
        }
    };

    private Streams ()
    {
    }

    /**
     * Copy the remaining content of one stream to the other
     *
     * @param in
     *            the input stream
     * @param out
     *            the output stream
     * @return the number of bytes copied
     * @throws IOException
     *             if any I/O error occurs
     */
    public static long copy ( final InputStream in, final OutputStream out ) throws IOException
    {
        Objects.requireNonNull ( in );
        Objects.requireNonNull ( out );

        final byte[] buffer = new byte[COPY_BUFFER_SIZE];
        long result = 0;

        int rc;
        while ( ( rc = in.read ( buffer ) ) >= 0 )
        {
            result += rc;
            out.write ( buffer, 0, rc );
        }

        return result;
    }

    public static InputStream closedInput ()
    {
        return CLOSED_INPUT;
    }

    public static OutputStream closedOutput ()
    {
        return CLOSED_OUTPUT;
    }
}
