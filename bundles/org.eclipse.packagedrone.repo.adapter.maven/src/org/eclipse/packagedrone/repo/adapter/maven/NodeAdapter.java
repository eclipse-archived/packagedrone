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
package org.eclipse.packagedrone.repo.adapter.maven;

import java.lang.reflect.Type;

import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.ArtifactNode;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.ContentNode;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.DataNode;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.DirectoryNode;
import org.eclipse.packagedrone.repo.adapter.maven.ChannelData.Node;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class NodeAdapter implements JsonSerializer<Node>, JsonDeserializer<Node>
{

    @Override
    public JsonElement serialize ( final Node node, final Type type, final JsonSerializationContext ctx )
    {
        if ( node == null )
        {
            return JsonNull.INSTANCE;
        }

        final JsonObject o = new JsonObject ();

        if ( node instanceof ContentNode && ! ( node instanceof DataNode ) )
        {
            final ContentNode cnode = (ContentNode)node;
            o.addProperty ( "type", DataNode.class.getSimpleName () );
            o.add ( "node", ctx.serialize ( new DataNode ( cnode.getData (), cnode.getMimeType () ) ) );
        }
        else
        {
            o.addProperty ( "type", node.getClass ().getSimpleName () );
            o.add ( "node", ctx.serialize ( node ) );
        }

        return o;
    }

    @Override
    public Node deserialize ( final JsonElement element, final Type type, final JsonDeserializationContext ctx ) throws JsonParseException
    {

        final JsonObject o = element.getAsJsonObject ();
        final String typeString = o.get ( "type" ).getAsString ();
        final JsonObject val = o.get ( "node" ).getAsJsonObject ();

        switch ( typeString )
        {
            case "DirectoryNode":
                return ctx.deserialize ( val, DirectoryNode.class );
            case "DataNode":
                return ctx.deserialize ( val, DataNode.class );
            case "ArtifactNode":
                return ctx.deserialize ( val, ArtifactNode.class );
            default:
                break;
        }

        return null;
    }

}
