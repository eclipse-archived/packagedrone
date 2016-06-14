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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream which does not forward the {@link #close()} call
 */
public class CloseShieldOutputStream extends FilterOutputStream
{
    public CloseShieldOutputStream ( final OutputStream out )
    {
        super ( out );
    }

    @Override
    public void close () throws IOException
    {
        this.out = Streams.closedOutput ();
    }
}
