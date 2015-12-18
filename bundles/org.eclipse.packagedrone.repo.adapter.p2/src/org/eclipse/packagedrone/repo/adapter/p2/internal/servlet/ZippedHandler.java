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
package org.eclipse.packagedrone.repo.adapter.p2.internal.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.p2.internal.aspect.ChannelStreamer;
import org.eclipse.packagedrone.repo.aspect.common.osgi.OsgiExtractor;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.servlet.Handler;

import com.google.common.io.ByteStreams;

public class ZippedHandler implements Handler
{
    private final ReadableChannel channel;

    private final Set<String> nameCache = new HashSet<> ();

    public ZippedHandler ( final ReadableChannel channel )
    {
        this.channel = channel;
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException
    {
        this.nameCache.clear ();

        resp.setContentType ( "application/zip" );
        final ZipOutputStream zos = new ZipOutputStream ( resp.getOutputStream () );

        final String title = this.channel.getInformation ().makeTitle ();

        final ChannelStreamer streamer = new ChannelStreamer ( title, this.channel.getMetaData (), false, true );

        for ( final ArtifactInformation a : this.channel.getContext ().getArtifacts ().values () )
        {
            streamer.process ( a, ( ai, receiver ) -> this.channel.getContext ().stream ( ai, receiver ) );

            final Map<MetaKey, String> md = a.getMetaData ();

            final String classifier = md.get ( OsgiExtractor.KEY_CLASSIFIER );
            final String symbolicName = md.get ( OsgiExtractor.KEY_NAME );
            final String version = md.get ( OsgiExtractor.KEY_VERSION );

            if ( classifier == null || symbolicName == null || version == null )
            {
                continue;
            }

            final String name = String.format ( "%s_%s.jar", symbolicName, version );

            switch ( classifier )
            {
                case "bundle":
                    stream ( zos, this.channel, a, "plugins/" + name );
                    break;
                case "eclipse.feature":
                    stream ( zos, this.channel, a, "features/" + name );
                    break;
            }
        }

        streamer.spoolOut ( ( id, name, mimeType, stream ) -> {
            zos.putNextEntry ( new ZipEntry ( name ) );
            stream.accept ( zos );
            zos.closeEntry ();
        } );

        zos.close ();
    }

    private void stream ( final ZipOutputStream zos, final ReadableChannel channel, final ArtifactInformation a, final String name ) throws IOException
    {
        if ( !this.nameCache.add ( name ) )
        {
            // duplicate entry
            return;
        }

        zos.putNextEntry ( new ZipEntry ( name ) );
        channel.getContext ().stream ( a, stream -> ByteStreams.copy ( stream, zos ) );
        zos.closeEntry ();
    }
}
