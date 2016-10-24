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

import java.io.IOException;
import java.io.InputStream;

public interface ContentProvider
{
    public static final ContentProvider NULL_CONTENT = new ContentProvider () {

        @Override
        public long getSize ()
        {
            return 0;
        }

        @Override
        public InputStream createInputStream () throws IOException
        {
            return null;
        }

        @Override
        public boolean hasContent ()
        {
            return false;
        }
    };

    public long getSize ();

    /**
     * Create a new input stream <br>
     * <em>Note:</em> The caller must close the stream
     *
     * @return a new input stream
     */
    public InputStream createInputStream () throws IOException;

    public boolean hasContent ();
}
