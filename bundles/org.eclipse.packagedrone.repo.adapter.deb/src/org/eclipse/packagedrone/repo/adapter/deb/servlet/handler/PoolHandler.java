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
package org.eclipse.packagedrone.repo.adapter.deb.servlet.handler;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.util.DownloadHelper;

public class PoolHandler
{
    private final ReadableChannel channel;

    private final String artifactId;

    private final String name;

    public PoolHandler ( final ReadableChannel channel, final String artifactId, final String name )
    {
        this.channel = channel;
        this.artifactId = artifactId;
        this.name = name;
    }

    public void process ( final HttpServletResponse response ) throws IOException
    {
        if ( this.artifactId == null || this.artifactId.isEmpty () )
        {
            response.setContentType ( "text/plain" );
            response.getWriter ().println ( "Browsing the pool is currently not supported" );
            return;
        }

        DownloadHelper.streamArtifact ( response, this.artifactId, Optional.empty (), true, this.channel, info -> this.name );
    }

}
