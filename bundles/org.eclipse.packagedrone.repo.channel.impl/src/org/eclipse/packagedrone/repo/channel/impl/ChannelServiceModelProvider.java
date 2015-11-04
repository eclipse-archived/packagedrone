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
package org.eclipse.packagedrone.repo.channel.impl;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.storage.apm.AbstractSimpleStorageModelProvider;
import org.eclipse.packagedrone.storage.apm.StorageContext;
import org.eclipse.packagedrone.storage.apm.util.ReplaceOnCloseWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        return context.getBasePath ().resolve ( "channels.json" );
    }

    protected Gson createGson ()
    {
        final GsonBuilder builder = new GsonBuilder ();
        builder.setPrettyPrinting ();
        builder.serializeNulls ();
        builder.setDateFormat ( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
        builder.registerTypeAdapter ( DeployGroup.class, new DeployGroupTypeAdapter () );
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
            return new ChannelServiceModify ( new ChannelServiceModel () );
        }
    }

}
