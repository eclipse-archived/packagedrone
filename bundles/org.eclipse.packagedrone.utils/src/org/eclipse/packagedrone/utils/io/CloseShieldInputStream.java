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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream which does not forward the {@link #close()} call
 */
public class CloseShieldInputStream extends FilterInputStream
{
    public CloseShieldInputStream ( final InputStream in )
    {
        super ( in );
    }

    @Override
    public void close () throws IOException
    {
        this.in = Streams.closedInput ();
    }
}
