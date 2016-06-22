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
package org.eclipse.packagedrone.sec.service.apm.model;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.packagedrone.storage.apm.AbstractSimpleStorageModelProvider;
import org.eclipse.packagedrone.storage.apm.StorageContext;
import org.eclipse.packagedrone.storage.apm.util.ReplaceOnCloseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UserStorageModelProvider extends AbstractSimpleStorageModelProvider<UserModel, UserWriteModel>
{
    public UserStorageModelProvider ()
    {
        super ( UserWriteModel.class );
    }

    private final static Logger logger = LoggerFactory.getLogger ( UserStorageModelProvider.class );

    @Override
    public UserWriteModel cloneWriteModel ( final UserWriteModel writeModel )
    {
        return new UserWriteModel ( writeModel );
    }

    @Override
    public UserModel makeViewModelTyped ( final UserWriteModel writeModel )
    {
        return new UserModel ( writeModel.getAll ().values () );
    }

    protected Gson createGson ()
    {
        return new GsonBuilder ().create ();
    }

    protected Path makePath ( final StorageContext context )
    {
        return context.getBasePath ().resolve ( "users.json" );
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final UserWriteModel writeModel ) throws Exception
    {
        if ( !writeModel.isChanged () )
        {
            logger.trace ( "Write model unchanged" );
            return;
        }

        final Path path = makePath ( context );
        logger.debug ( "Persisting model: {}", path.toAbsolutePath () );

        try ( ReplaceOnCloseWriter writer = new ReplaceOnCloseWriter ( path, StandardCharsets.UTF_8 ) )
        {
            createGson ().toJson ( writeModel.asCollection (), writer );

            writer.commit ();
        }
    }

    @Override
    protected UserWriteModel loadWriteModel ( final StorageContext context ) throws Exception
    {
        try ( BufferedReader reader = Files.newBufferedReader ( makePath ( context ) ) )
        {
            final UserEntity[] users = createGson ().fromJson ( reader, UserEntity[].class );
            return new UserWriteModel ( Arrays.asList ( users ), false );
        }
        catch ( final NoSuchFileException e )
        {
            return new UserWriteModel ();
        }
    }

}
