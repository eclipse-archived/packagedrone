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
package org.eclipse.packagedrone.repo.channel.impl;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.eclipse.packagedrone.storage.apm.AbstractSimpleStorageModelProvider;
import org.eclipse.packagedrone.storage.apm.StorageContext;
import org.eclipse.packagedrone.storage.apm.util.ReplaceOnCloseWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ChannelInstanceModelProvider extends AbstractSimpleStorageModelProvider<ChannelInstanceModelAccess, ChannelInstanceModel>
{
    private final String channelId;

    public ChannelInstanceModelProvider ( final String channelId )
    {
        super ( ChannelInstanceModelAccess.class, ChannelInstanceModel.class );
        this.channelId = channelId;
    }

    @Override
    protected ChannelInstanceModelAccess makeViewModelTyped ( final ChannelInstanceModel writeModel )
    {
        return new ChannelInstanceModel ( writeModel );
    }

    @Override
    protected ChannelInstanceModel cloneWriteModel ( final ChannelInstanceModel writeModel )
    {
        return new ChannelInstanceModel ( writeModel );
    }

    private static Gson createGson ()
    {
        final GsonBuilder gb = new GsonBuilder ();
        gb.setPrettyPrinting ();
        return gb.create ();
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final ChannelInstanceModel writeModel ) throws Exception
    {
        if ( !writeModel.isModified () )
        {
            return;
        }

        final Gson gson = createGson ();

        final Path path = makePath ( context );
        Files.createDirectories ( path.getParent () );

        try ( ReplaceOnCloseWriter writer = new ReplaceOnCloseWriter ( path, StandardCharsets.UTF_8 ) )
        {
            gson.toJson ( writeModel, writer );
            writer.commit ();
        }
    }

    @Override
    protected ChannelInstanceModel loadWriteModel ( final StorageContext context ) throws Exception
    {
        final Path path = makePath ( context );

        final Gson gson = createGson ();
        try ( Reader reader = Files.newBufferedReader ( path ) )
        {
            return gson.fromJson ( reader, ChannelInstanceModel.class );
        }
        catch ( final NoSuchFileException e )
        {
            return new ChannelInstanceModel (); // create a new one
        }
    }

    private Path makePath ( final StorageContext context )
    {
        return context.getBasePath ().resolve ( "channel.cfg" ).resolve ( this.channelId + ".json" );
    }
}
