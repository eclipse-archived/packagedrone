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
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;

import com.google.common.io.ByteStreams;

public class ChannelCacheHandler implements Handler
{
    private final ReadableChannel channel;

    private final MetaKey key;

    public ChannelCacheHandler ( final ReadableChannel channel, final MetaKey key )
    {
        this.channel = channel;
        this.key = key;
    }

    @Override
    public void process ( final OutputStream stream ) throws IOException
    {
        this.channel.streamCacheEntry ( this.key, entry -> ByteStreams.copy ( entry.getStream (), stream ) );
    }

    @Override
    public void process ( final HttpServletResponse response ) throws IOException
    {
        if ( !this.channel.streamCacheEntry ( this.key, entry -> {
            response.setContentType ( entry.getMimeType () );
            response.setContentLengthLong ( entry.getSize () );
            ByteStreams.copy ( entry.getStream (), response.getOutputStream () );
        } ) )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.getWriter ().format ( "Content '%s' not found.%n", this.key );
        }
    }
}
