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
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Path;

public class ReplaceOnCloseWriter extends OutputStreamWriter
{
    private final ReplaceOnCloseOutputStream out;

    @SuppressWarnings ( "resource" )
    public ReplaceOnCloseWriter ( final Path path, final Charset cs ) throws IOException
    {
        this ( new ReplaceOnCloseOutputStream ( path ), cs );
    }

    public ReplaceOnCloseWriter ( final ReplaceOnCloseOutputStream out, final Charset cs )
    {
        super ( out, cs );
        this.out = out;
    }

    public ReplaceOnCloseWriter ( final ReplaceOnCloseOutputStream out, final CharsetEncoder enc )
    {
        super ( out, enc );
        this.out = out;
    }

    public ReplaceOnCloseWriter ( final ReplaceOnCloseOutputStream out )
    {
        super ( out );
        this.out = out;
    }

    public void commit ()
    {
        this.out.commit ();
    }
}
