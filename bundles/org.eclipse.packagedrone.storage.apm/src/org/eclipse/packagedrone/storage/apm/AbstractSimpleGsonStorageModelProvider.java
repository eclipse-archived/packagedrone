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
package org.eclipse.packagedrone.storage.apm;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;

import org.eclipse.packagedrone.storage.apm.util.ReplaceOnCloseWriter;
import org.eclipse.packagedrone.utils.gson.InstantTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * An abstract base for creating storage model providers which are based on a
 * simple JSON model
 *
 * @param <V>
 *            the view model
 * @param <W>
 *            the write model
 * @param <G>
 *            the GSON persistence model
 */
public abstract class AbstractSimpleGsonStorageModelProvider<V, W, G> extends AbstractSimpleStorageModelProvider<V, W>
{
    protected final Class<G> gsonModelClass;

    public AbstractSimpleGsonStorageModelProvider ( final Class<W> writeClazz, final Class<G> gsonModelClass )
    {
        super ( writeClazz );
        this.gsonModelClass = gsonModelClass;
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final W writeModel ) throws Exception
    {
        try ( ReplaceOnCloseWriter writer = new ReplaceOnCloseWriter ( makePath ( context ), StandardCharsets.UTF_8 ) )
        {
            createGson ().toJson ( toGsonModel ( writeModel ), writer );
            writer.commit ();
        }
    }

    protected abstract G toGsonModel ( final W writeModel );

    protected abstract W fromGsonModel ( final G gsonModel );

    protected Gson createGson ()
    {
        final GsonBuilder gb = new GsonBuilder ();

        gb.setPrettyPrinting ();
        gb.registerTypeAdapter ( Instant.class, InstantTypeAdapter.DEFAULT_INSTANCE );

        return gb.create ();
    }

    @Override
    protected W loadWriteModel ( final StorageContext context ) throws Exception
    {
        try ( Reader reader = Files.newBufferedReader ( makePath ( context ) ) )
        {
            return fromGsonModel ( createGson ().fromJson ( reader, this.gsonModelClass ) );
        }
        catch ( final NoSuchFileException e )
        {
            return createNewModel ();
        }
    }

    protected abstract W createNewModel ();

    protected abstract Path makePath ( final StorageContext context );
}
