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
package org.eclipse.packagedrone.repo.channel.apm;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.apm.internal.Finally;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore.Transaction;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore;
import org.eclipse.packagedrone.repo.channel.provider.AccessContext;
import org.eclipse.packagedrone.storage.apm.AbstractSimpleStorageModelProvider;
import org.eclipse.packagedrone.storage.apm.StorageContext;
import org.eclipse.packagedrone.storage.apm.util.ReplaceOnCloseOutputStream;
import org.eclipse.packagedrone.utils.Suppressed;
import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.LongSerializationPolicy;

public class ChannelModelProvider extends AbstractSimpleStorageModelProvider<AccessContext, ModifyContextImpl>
{
    private final static Logger logger = LoggerFactory.getLogger ( ChannelModelProvider.class );

    private final String channelId;

    private BlobStore store;

    private CacheStore cacheStore;

    private final EventAdmin eventAdmin;

    private final String dir;

    public ChannelModelProvider ( final EventAdmin eventAdmin, final String channelId, final String dir )
    {
        super ( AccessContext.class, ModifyContextImpl.class );

        this.eventAdmin = eventAdmin;
        this.channelId = channelId;
        this.dir = dir;
    }

    @Override
    public void start ( final StorageContext context ) throws Exception
    {
        this.store = new BlobStore ( makeBasePath ( context, this.dir ).resolve ( "blobs" ) );
        this.cacheStore = new CacheStore ( makeBasePath ( context, this.dir ).resolve ( "cache" ) );
        super.start ( context );
    }

    @Override
    public void stop ()
    {
        super.stop ();
        this.store.close ();
        this.cacheStore.close ();
    }

    @Override
    protected AccessContext makeViewModelTyped ( final ModifyContextImpl writeModel )
    {
        return writeModel;
    }

    @Override
    protected ModifyContextImpl cloneWriteModel ( final ModifyContextImpl writeModel )
    {
        return new ModifyContextImpl ( writeModel );
    }

    public static Path makeBasePath ( final StorageContext context, final String dir )
    {
        return context.getBasePath ().resolve ( Paths.get ( "channels", dir ) );
    }

    public static Path makeStatePath ( final StorageContext context, final String dir )
    {
        return makeBasePath ( context, dir ).resolve ( "state.json" );
    }

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    static Gson createGson ()
    {
        final GsonBuilder builder = new GsonBuilder ();

        builder.setPrettyPrinting ();
        builder.setLongSerializationPolicy ( LongSerializationPolicy.STRING );
        builder.setDateFormat ( DATE_FORMAT );
        builder.registerTypeAdapter ( MetaKey.class, new JsonDeserializer<MetaKey> () {

            @Override
            public MetaKey deserialize ( final JsonElement json, final Type type, final JsonDeserializationContext ctx ) throws JsonParseException
            {
                return MetaKey.fromString ( json.getAsString () );
            }
        } );

        return builder.create ();
    }

    @Override
    public void closeWriteModel ( final ModifyContextImpl model )
    {
        final BlobStore.Transaction blobTransaction = model.claimTransaction ();
        final CacheStore.Transaction cacheTransaction = model.claimCacheTransaction ();

        try ( final Suppressed<RuntimeException> s = new Suppressed<> ( "Failed to close write model", RuntimeException::new ) )
        {
            if ( blobTransaction != null )
            {
                s.run ( blobTransaction::rollback );
            }
            if ( cacheTransaction != null )
            {
                s.run ( cacheTransaction::rollback );
            }
        }
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final ModifyContextImpl writeModel ) throws Exception
    {
        try ( Handle h1 = Profile.start ( this, "persistWriteModel" ) )
        {
            final AtomicReference<Transaction> t = new AtomicReference<> ( writeModel.claimTransaction () );
            final AtomicReference<CacheStore.Transaction> ct = new AtomicReference<> ( writeModel.claimCacheTransaction () );

            final Finally f = new Finally ();

            f.add ( () -> {
                final Transaction v = t.get ();
                if ( v != null )
                {
                    v.rollback ();
                }
            } );

            f.add ( () -> {
                final CacheStore.Transaction v = ct.get ();
                if ( v != null )
                {
                    v.rollback ();
                }
            } );

            try
            {
                final Path path = makeStatePath ( context, this.dir );
                Files.createDirectories ( path.getParent () );

                // commit blob store

                if ( t.get () != null )
                {
                    t.get ().commit ();
                    t.set ( null );
                }

                // write model

                try ( Handle h2 = Profile.start ( this, "persistWriteModel#write" ) )
                {
                    try ( ReplaceOnCloseOutputStream stream = new ReplaceOnCloseOutputStream ( path );
                          ChannelWriter writer = new ChannelWriter ( stream ); )
                    {
                        writer.write ( writeModel );
                        stream.commit ();
                    }
                }

                // commit cache store

                if ( ct.get () != null )
                {
                    ct.get ().commit ();
                    ct.set ( null );
                }
            }
            catch ( final Exception e )
            {
                logger.warn ( "Failed to persist model", e );
                throw e;
            }
            finally
            {
                try ( Handle h2 = Profile.start ( this, "persistWriteModel#finally" ) )
                {
                    f.runAll ();
                }
            }
        }
    }

    @Override
    protected ModifyContextImpl loadWriteModel ( final StorageContext context ) throws Exception
    {
        final Path path = makeStatePath ( context, this.dir );

        try ( InputStream stream = new BufferedInputStream ( Files.newInputStream ( path ) );
              ChannelReader reader = new ChannelReader ( stream, this.channelId, this.eventAdmin, this.store, this.cacheStore ); )
        {
            final ModifyContextImpl model = reader.read ();
            if ( model == null )
            {
                // FIXME: handle broken channel state
                throw new IllegalStateException ( "Unable to load channel model" );
            }
            return model;
        }
        catch ( final NoSuchFileException e )
        {
            // create a new model

            final ChannelModel model = new ChannelModel ();

            model.setCreationTimestamp ( new Date () );

            return new ModifyContextImpl ( this.channelId, this.eventAdmin, this.store, this.cacheStore );
        }
    }

}
