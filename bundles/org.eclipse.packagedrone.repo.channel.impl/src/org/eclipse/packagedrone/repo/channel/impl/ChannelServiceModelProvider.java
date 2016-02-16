/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.impl;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.impl.model.ChannelConfiguration;
import org.eclipse.packagedrone.repo.gson.MetaKeyTypeAdapter;
import org.eclipse.packagedrone.storage.apm.AbstractSimpleStorageModelProvider;
import org.eclipse.packagedrone.storage.apm.StorageContext;
import org.eclipse.packagedrone.storage.apm.util.ReplaceOnCloseWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ChannelServiceModelProvider extends AbstractSimpleStorageModelProvider<ChannelServiceAccess, ChannelServiceModify>
{
    public ChannelServiceModelProvider ()
    {
        super ( ChannelServiceAccess.class, ChannelServiceModify.class );
    }

    @Override
    protected ChannelServiceAccess makeViewModelTyped ( final ChannelServiceModify writeModel )
    {
        return writeModel;
    }

    @Override
    protected ChannelServiceModify cloneWriteModel ( final ChannelServiceModify writeModel )
    {
        return new ChannelServiceModify ( writeModel );
    }

    private Path makePath ( final StorageContext context )
    {
        return context.getBasePath ().resolve ( "channels.v2.json" );
    }

    protected Gson createGson ()
    {
        final GsonBuilder builder = new GsonBuilder ();
        builder.setPrettyPrinting ();
        builder.serializeNulls ();
        builder.setDateFormat ( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
        builder.registerTypeAdapter ( DeployGroup.class, new DeployGroupTypeAdapter () );
        builder.registerTypeAdapter ( MetaKey.class, MetaKeyTypeAdapter.INSTANCE );
        return builder.create ();
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final ChannelServiceModify writeModel ) throws Exception
    {
        try ( final ReplaceOnCloseWriter writer = new ReplaceOnCloseWriter ( makePath ( context ), StandardCharsets.UTF_8 ) )
        {
            createGson ().toJson ( writeModel.getModel (), writer );

            writer.commit ();
        }
    }

    @Override
    protected ChannelServiceModify loadWriteModel ( final StorageContext context ) throws Exception
    {
        try ( Reader reader = Files.newBufferedReader ( makePath ( context ), StandardCharsets.UTF_8 ) )
        {
            final ChannelServiceModel model = createGson ().fromJson ( reader, ChannelServiceModel.class );
            return new ChannelServiceModify ( model != null ? model : new ChannelServiceModel () );
        }
        catch ( final NoSuchFileException e )
        {
            return tryMigrate ( context );
        }
    }

    private ChannelServiceModify tryMigrate ( final StorageContext context ) throws IOException
    {
        final Path v1 = context.getBasePath ().resolve ( "channels.json" );
        if ( Files.exists ( v1 ) )
        {
            return loadV1 ( context, v1 );
        }

        // empty model

        return new ChannelServiceModify ( new ChannelServiceModel () );
    }

    private ChannelServiceModify loadV1 ( final StorageContext context, final Path v1 ) throws IOException
    {
        JsonElement root;
        try ( Reader r = Files.newBufferedReader ( v1 ) )
        {
            root = new JsonParser ().parse ( r );
        }

        final JsonElement nameMap = root.getAsJsonObject ().get ( "nameMap" );
        root.getAsJsonObject ().add ( "nameMap", transformV1NameMap ( nameMap.getAsJsonObject () ) );

        final JsonObject channels = new JsonObject ();
        root.getAsJsonObject ().add ( "channels", channels );
        discoverChannels ( context, channels );

        final ChannelServiceModel model = createGson ().fromJson ( root, ChannelServiceModel.class );
        return new ChannelServiceModify ( model != null ? model : new ChannelServiceModel () );
    }

    private void discoverChannels ( final StorageContext context, final JsonObject channels ) throws IOException
    {
        final Path channelsBase = context.getBasePath ().resolve ( "channels" );
        if ( !Files.isDirectory ( channelsBase ) )
        {
            return;
        }

        Files.list ( channelsBase ).filter ( dir -> {
            if ( !Files.isDirectory ( dir ) )
            {
                return false;
            }

            if ( !Files.exists ( dir.resolve ( "state.json" ) ) )
            {
                return false;
            }
            return true;
        } ).forEach ( dir -> {
            try
            {
                addApmChannel ( channels, dir );
            }
            catch ( final IOException e )
            {
                // if we can't convert, don't continue
                throw new RuntimeException ( e );
            }
        } );
    }

    private void addApmChannel ( final JsonObject channels, final Path dir ) throws IOException
    {
        final String id = "apm_" + dir.getFileName ().toString ();

        JsonObject root;
        try ( Reader r = Files.newBufferedReader ( dir.resolve ( "state.json" ) ) )
        {
            root = (JsonObject)new JsonParser ().parse ( r );
        }

        String description = null;
        if ( root.has ( "description" ) && root.get ( "description" ).isJsonPrimitive () )
        {
            description = root.get ( "description" ).getAsString ();
        }

        final ChannelConfiguration cfg = new ChannelConfiguration ();
        cfg.setProviderId ( "apm" );
        cfg.setDescription ( description );
        cfg.getConfiguration ().put ( new MetaKey ( "apm", "dir-override" ), dir.getFileName ().toString () );

        channels.add ( id, createGson ().toJsonTree ( cfg ) );
    }

    private JsonElement transformV1NameMap ( final JsonObject nameMap )
    {
        final JsonObject result = new JsonObject ();

        for ( final Map.Entry<String, JsonElement> entry : nameMap.entrySet () )
        {
            final String key = entry.getKey ();
            final JsonArray arr = new JsonArray ();
            arr.add ( entry.getValue () );
            result.add ( key, arr );
        }

        return result;
    }

}
