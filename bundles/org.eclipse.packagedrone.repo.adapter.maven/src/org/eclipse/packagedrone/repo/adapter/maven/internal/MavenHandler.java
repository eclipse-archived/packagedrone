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
package org.eclipse.packagedrone.repo.adapter.maven.internal;

import static java.util.Optional.empty;
import static org.eclipse.packagedrone.repo.channel.util.DownloadHelper.streamArtifact;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.ArtifactNode;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.ContentNode;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.DataNode;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.DirectoryNode;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.Node;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.scada.utils.str.ExtendedPropertiesReplacer;
import org.eclipse.scada.utils.str.StringReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.html.HtmlEscapers;
import com.google.common.io.CharStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MavenHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( MavenHandler.class );

    private final ChannelData channelData;

    private final ReadableChannel channel;

    public MavenHandler ( final ReadableChannel channel, final ChannelData channelData )
    {
        this.channel = channel;
        this.channelData = channelData;
    }

    public void handle ( final String path, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        final LinkedList<String> segs = path != null ? new LinkedList<> ( Arrays.asList ( path.split ( "/+" ) ) ) : new LinkedList<> ();

        final Node node = this.channelData.findNode ( segs );
        if ( node == null )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.setContentType ( "text/plain" );
            response.getWriter ().format ( "Unable to find: '%s'", path == null ? "" : path );
            return;
        }

        logger.debug ( "{} : Node - {}", path, node );

        if ( node instanceof DirectoryNode )
        {
            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                response.sendRedirect ( request.getRequestURI () + "/" );
                return;
            }

            response.setStatus ( HttpServletResponse.SC_OK );

            final String acceptHeader = request.getHeader ( "Accept" );
            if ( acceptHeader != null && acceptHeader.contains ( "application/json" ) )
            {
                renderDirAsJson ( response, node );
            }
            else
            {
                renderDirAsHtml ( response, (DirectoryNode)node, path );
            }
        }
        else if ( node instanceof ContentNode )
        {
            final ContentNode dataNode = (ContentNode)node;

            response.setStatus ( HttpServletResponse.SC_OK );
            response.setContentType ( dataNode.getMimeType () );
            response.setContentLength ( dataNode.getData ().length );
            response.getOutputStream ().write ( dataNode.getData () );
        }
        else if ( node instanceof ArtifactNode )
        {
            response.setStatus ( HttpServletResponse.SC_OK );

            download ( response, (ArtifactNode)node );
        }
        else
        {
            response.setStatus ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            response.setContentType ( "text/plain" );
            response.getWriter ().format ( "Unknown node type: %s", node == null ? "null" : node.getClass ().getName () );
        }
    }

    private void download ( final HttpServletResponse response, final ArtifactNode node ) throws IOException
    {
        streamArtifact ( response, node.getArtifactId (), empty (), false, this.channel, null );
    }

    private void renderDirAsJson ( final HttpServletResponse response, final Node node ) throws IOException
    {
        final GsonBuilder gsonBuilder = new GsonBuilder ();
        gsonBuilder.registerTypeAdapter ( DataNode.class, new JsonSerializer<DataNode> () {

            @Override
            public JsonElement serialize ( final DataNode src, final Type typeOfSrc, final JsonSerializationContext context )
            {
                final JsonObject jsonObject = new JsonObject ();
                // final String data = new String ( src.getData () );
                // jsonObject.addProperty("data", data);
                jsonObject.addProperty ( "mimeType", src.getMimeType () );
                return jsonObject;
            }
        } );
        response.setContentType ( "application/json" );
        gsonBuilder.create ().toJson ( node, response.getWriter () );
    }

    private static class HtmlDirRenderer
    {
        private final DirectoryNode dir;

        public HtmlDirRenderer ( final DirectoryNode dir )
        {
            this.dir = dir;
        }

        @Override
        public String toString ()
        {
            final StringWriter sw = new StringWriter ();
            final PrintWriter pw = new PrintWriter ( sw );

            render ( pw );

            pw.close ();
            return sw.toString ();
        }

        private void render ( final PrintWriter pw )
        {
            pw.write ( "<ul>\n" );

            pw.write ( "<li><a href=\"..\">..</a></li>" );

            final List<String> dirs = new ArrayList<> ( this.dir.getNodes ().keySet () );
            Collections.sort ( dirs );

            for ( final String entry : dirs )
            {
                final Node node = this.dir.getNodes ().get ( entry );

                String esc = HtmlEscapers.htmlEscaper ().escape ( entry );
                pw.write ( "<li><a href=\"" );

                if ( node.isDirectory () && !esc.endsWith ( "/" ) )
                {
                    // ensure it ends with /
                    esc = esc + "/";
                }

                pw.write ( esc );
                pw.write ( "\">" );
                pw.write ( esc );
                pw.write ( "</a></li>\n" );
            }
            pw.write ( "</ul>\n" );
        }
    }

    private void renderDirAsHtml ( final HttpServletResponse response, final DirectoryNode dir, final String path ) throws IOException
    {
        response.setContentType ( "text/html; charset=utf-8" );

        @SuppressWarnings ( "resource" )
        final PrintWriter w = response.getWriter ();

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "path", path );
        model.put ( "dir", new HtmlDirRenderer ( dir ) );
        model.put ( "version", VersionInformation.VERSION );
        w.write ( StringReplacer.replace ( loadResource ( "content/index.html" ), new ExtendedPropertiesReplacer ( model ), StringReplacer.DEFAULT_PATTERN, true ) );
    }

    private String loadResource ( final String name ) throws IOException
    {
        try ( InputStream is = MavenHandler.class.getResourceAsStream ( name );
              Reader r = new InputStreamReader ( is, StandardCharsets.UTF_8 ) )
        {
            return CharStreams.toString ( r );
        }
    }

}
