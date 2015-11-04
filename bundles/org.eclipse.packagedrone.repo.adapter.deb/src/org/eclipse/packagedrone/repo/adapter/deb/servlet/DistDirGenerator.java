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

public class DistDirGenerator
{

    private final ChannelConfiguration cfg;

    public DistDirGenerator ( final ChannelConfiguration cfg )
    {
        this.cfg = cfg;
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        if ( this.cfg.getSigningService () != null )
        {
            sb.append ( "<li><a href=\"InRelease\">InRelease</a></li>" );
            sb.append ( "<li><a href=\"Release.gpg\">Release.gpg</a></li>" );
        }
        sb.append ( "<li><a href=\"Release\">Release</a></li>" );

        /*
         * TODO: implement multiple components
        for ( final String comp : this.cfg.getComponents () )
        {
            sb.append ( "<li><a href=\"" + comp + "\">" + comp + "</li>" );
        }
        */
        sb.append ( "<li><a href=\"" + this.cfg.getDefaultComponent () + "\">" + this.cfg.getDefaultComponent () + "</li>" );

        return sb.toString ();
    }
}
