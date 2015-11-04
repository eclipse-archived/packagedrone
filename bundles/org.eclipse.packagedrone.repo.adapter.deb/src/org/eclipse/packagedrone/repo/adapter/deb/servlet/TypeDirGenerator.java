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

public class TypeDirGenerator
{
    public TypeDirGenerator ( final ChannelConfiguration cfg )
    {
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( "<li><a href=\"Release\">Release</a></li>" );
        sb.append ( "<li><a href=\"Packages\">Packages</a></li>" );
        sb.append ( "<li><a href=\"Packages.gz\">Packages.gz</a></li>" );
        sb.append ( "<li><a href=\"Packages.bz2\">Packages.bz2</a></li>" );

        return sb.toString ();
    }
}
