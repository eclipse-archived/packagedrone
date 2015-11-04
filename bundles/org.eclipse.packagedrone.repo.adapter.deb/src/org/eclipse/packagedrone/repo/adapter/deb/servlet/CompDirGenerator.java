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
package org.eclipse.packagedrone.repo.adapter.deb.servlet;

import org.eclipse.packagedrone.repo.adapter.deb.ChannelConfiguration;

public class CompDirGenerator
{

    private final ChannelConfiguration cfg;

    public CompDirGenerator ( final ChannelConfiguration cfg )
    {
        this.cfg = cfg;
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        if ( this.cfg.getArchitectures () != null )
        {
            for ( final String arch : this.cfg.getArchitectures () )
            {
                sb.append ( String.format ( "<li><a href=\"binary-%1$s\">binary-%1$s</a></li>", arch ) );
            }
        }

        // sb.append ( "<li><a href=\"source\">source</a></li>" );

        return sb.toString ();
    }
}
