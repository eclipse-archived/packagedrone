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

public final class FileNames
{
    private FileNames ()
    {
    }

    /**
     * Get the last segment of a path, the filename
     *
     * @param name
     *            the name to process
     * @return the last segment, or {@code null} if the input was {@code null}
     */
    public static String getBasename ( final String name )
    {
        if ( name == null )
        {
            return null;
        }

        final String[] toks = name.split ( "/" );
        if ( toks.length < 1 )
        {
            return name;
        }

        return toks[toks.length - 1];
    }
}
