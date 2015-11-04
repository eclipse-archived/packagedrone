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

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntry;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.channel.IdTransformer;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.repo.channel.ChannelState.Builder;
import org.eclipse.packagedrone.repo.channel.apm.aspect.AspectContextImpl;
import org.eclipse.packagedrone.repo.channel.apm.aspect.AspectMapModel;
import org.eclipse.packagedrone.repo.channel.apm.aspect.AspectableContext;
import org.eclipse.packagedrone.repo.channel.apm.internal.Activator;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore.Transaction;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;
import org.eclipse.packagedrone.repo.utils.IOConsumer;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.utils.Exceptions;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class ModifyContextImpl implements ModifyContext, AspectableContext
{
    private final String localChannelId;

    private final EventAdmin eventAdmin;

    private final BlobStore store;

    private final CacheStore cacheStore;

    private final ChannelModel model;

    private final Map<String, ArtifactInformation> artifacts;

    private final Map<String, ArtifactInformation> modArtifacts;

    private final Map<MetaKey, CacheEntryInformation> cacheEntries;

    private final Map<MetaKey, CacheEntryInformation> modCacheEntries;

    private final Builder state;

    private Transaction transaction;

    private CacheStore.Transaction cacheTransaction;

    private final AspectContextImpl aspectContext;

    private SortedMap<MetaKey, String> metaDataCache;

    private IdTransformer idTransformer;

    public ModifyContextImpl ( final String localChannelId, final EventAdmin eventAdmin, final BlobStore store, final CacheStore cacheStore, final ChannelModel other )
    {
        this.localChannelId = localChannelId;

        this.eventAdmin = eventAdmin;
        this.store = store;
        this.cacheStore = cacheStore;

        this.model = new ChannelModel ( other );

        this.modArtifacts = new HashMap<> ( other.getArtifacts ().size () );
        for ( final Map.Entry<String, ArtifactModel> am : other.getArtifacts ().entrySet () )
        {
            final ArtifactInformation art = ArtifactModel.toInformation ( am );
            this.modArtifacts.put ( art.getId (), art );
        }
        this.artifacts = Collections.unmodifiableMap ( this.modArtifacts );

        this.state = new ChannelState.Builder ();
        this.state.setDescription ( other.getDescription () );
        this.state.setNumberOfArtifacts ( this.modArtifacts.size () );
        this.state.setNumberOfBytes ( this.modArtifacts.values ().stream ().mapToLong ( ArtifactInformation::getSize ).sum () );
        this.state.setValidationMessages ( other.getValidationMessages ().stream ().map ( ValidationMessageModel::toMessage ).collect ( Collectors.toList () ) );

        this.modCacheEntries = new HashMap<> ( other.getCacheEntries ().size () );
        for ( final Map.Entry<MetaKey, CacheEntryModel> cm : other.getCacheEntries ().entrySet () )
        {
            this.modCacheEntries.put ( cm.getKey (), CacheEntryModel.toEntry ( cm.getKey (), cm.getValue () ) );
        }
        this.cacheEntries = Collections.unmodifiableMap ( this.modCacheEntries );

        this.aspectContext = new AspectContextImpl ( this, Activator.getProcessor () );
    }

    public ModifyContextImpl ( final ModifyContextImpl other )
    {
        this ( other.localChannelId, other.eventAdmin, other.store, other.cacheStore, other.getModel () );

        // FIXME: prevent unnecessary copies
    }

    public void setIdTransformer ( final IdTransformer idTransformer )
    {
        this.idTransformer = idTransformer;
    }

    public ChannelModel getModel ()
    {
        return this.model;
    }

    @Override
    public ChannelState getState ()
    {
        return this.state.build (); // will only create a new instance when necessary
    }

    @Override
    public String getChannelId ()
    {
        if ( this.idTransformer == null )
        {
            throw new IllegalStateException ( "'idTransformer' was not set, it is required to get the external channel id" );
        }

        return this.idTransformer.transform ( this.localChannelId );
    }

    @Override
    public SortedMap<MetaKey, String> getMetaData ()
    {
        if ( this.metaDataCache == null )
        {
            final TreeMap<MetaKey, String> tmp = new TreeMap<> ();
            if ( this.model.getExtractedMetaData () != null )
            {
                tmp.putAll ( this.model.getExtractedMetaData () );
            }
            if ( this.model.getProvidedMetaData () != null )
            {
                tmp.putAll ( this.model.getProvidedMetaData () );
            }
            this.metaDataCache = Collections.unmodifiableSortedMap ( tmp );
        }
        return this.metaDataCache;
    }

    @Override
    public Map<String, ArtifactInformation> getArtifacts ()
    {
        return this.artifacts;
    }

    @Override
    public void setDetails ( final ChannelDetails details )
    {
        this.state.setDescription ( details.getDescription () );
        this.model.setDescription ( details.getDescription () );
    }

    @Override
    public Map<MetaKey, CacheEntryInformation> getCacheEntries ()
    {
        return this.cacheEntries;
    }

    @Override
    public SortedMap<String, String> getAspectStates ()
    {
        return this.aspectContext.getAspectStates ();
    }

    @Override
    public void applyMetaData ( final Map<MetaKey, String> changes )
    {
        testLocked ();

        for ( final Map.Entry<MetaKey, String> entry : changes.entrySet () )
        {
            final MetaKey key = entry.getKey ();
            final String value = entry.getValue ();

            if ( value == null )
            {
                this.model.getProvidedMetaData ().remove ( key );
            }
            else
            {
                this.model.getProvidedMetaData ().put ( key, value );
            }
        }

        // clear cache

        this.metaDataCache = null;

        // re-aggregate

        this.aspectContext.aggregate ();
    }

    @Override
    public void applyMetaData ( final String artifactId, final Map<MetaKey, String> changes )
    {
        testLocked ();

        final ArtifactModel artifact = this.model.getArtifacts ().get ( artifactId );
        if ( artifact == null )
        {
            throw new IllegalStateException ( String.format ( "Artifact '%s' is unknown", artifactId ) );
        }

        if ( !artifact.getFacets ().contains ( "stored" ) )
        {
            throw new IllegalStateException ( String.format ( "Artifact '%s' is not 'stored'", artifactId ) );
        }

        for ( final Map.Entry<MetaKey, String> entry : changes.entrySet () )
        {
            final MetaKey key = entry.getKey ();
            final String value = entry.getValue ();
            if ( value == null )
            {
                artifact.getProvidedMetaData ().remove ( key );
            }
            else
            {
                artifact.getProvidedMetaData ().put ( key, value );
            }
        }

        // update

        updateArtifact ( artifactId, artifact );

        if ( artifact.getFacets ().contains ( "generator" ) )
        {
            this.aspectContext.regenerate ( artifactId );
        }

        // TODO: update generic artifacts
    }

    private void testLocked ()
    {
        if ( this.model.isLocked () )
        {
            throw new IllegalStateException ( "Channel is locked" );
        }
    }

    @Override
    public void lock ()
    {
        this.state.setLocked ( true );
        this.model.setLocked ( true );
    }

    @Override
    public void unlock ()
    {
        this.state.setLocked ( false );
        this.model.setLocked ( false );
    }

    private void ensureTransaction ()
    {
        if ( this.transaction == null )
        {
            this.transaction = this.store.start ();
        }
    }

    private void ensureCacheTransaction ()
    {
        if ( this.cacheTransaction == null )
        {
            this.cacheTransaction = this.cacheStore.startTransaction ();
            this.modCacheEntries.clear ();
            this.model.getCacheEntries ().clear ();
        }
    }

    public Transaction claimTransaction ()
    {
        final Transaction t = this.transaction;
        this.transaction = null;
        return t;
    }

    public CacheStore.Transaction claimCacheTransaction ()
    {
        final CacheStore.Transaction t = this.cacheTransaction;
        this.cacheTransaction = null;
        return t;
    }

    @Override
    public ArtifactInformation createArtifact ( final InputStream source, final String name, final Map<MetaKey, String> providedMetaData )
    {
        return createArtifact ( null, source, name, providedMetaData );
    }

    @Override
    public ArtifactInformation createArtifact ( final String parentId, final InputStream source, final String name, final Map<MetaKey, String> providedMetaData )
    {
        testLocked ();

        if ( parentId != null )
        {
            final ArtifactInformation parent = this.modArtifacts.get ( parentId );
            if ( parent != null ) // we only check if the parent is there, a missing parent will be checked later on
            {
                if ( !parent.is ( "stored" ) )
                {
                    throw new IllegalArgumentException ( String.format ( "Unable to use artifact %s as parent, it is not a stored artifact", parentId ) );
                }
            }
        }

        return Exceptions.wrapException ( () -> this.aspectContext.createArtifact ( parentId, source, name, providedMetaData ) );
    }

    @Override
    public ArtifactInformation createGeneratorArtifact ( final String generatorId, final InputStream source, final String name, final Map<MetaKey, String> providedMetaData )
    {
        testLocked ();

        return Exceptions.wrapException ( () -> this.aspectContext.createGeneratorArtifact ( generatorId, source, name, providedMetaData ) );
    }

    @Override
    public ArtifactInformation createPlainArtifact ( final String parentId, final InputStream source, final String name, final Map<MetaKey, String> providedMetaData, final Set<String> facets, final String virtualizerAspectId )
    {
        ensureTransaction ();

        final String id = UUID.randomUUID ().toString ();

        try
        {
            final long size = this.transaction.create ( id, source );

            final ArtifactModel parent;

            // validate parent

            if ( parentId != null )
            {
                parent = this.model.getArtifacts ().get ( parentId );
                if ( parent == null )
                {
                    throw new IllegalArgumentException ( String.format ( "Parent artifact %s does not exists", parentId ) );
                }
            }
            else
            {
                parent = null;
            }

            final ArtifactInformation ai = new ArtifactInformation ( id, parentId, Collections.emptySet (), name, size, Instant.now (), facets, Collections.emptyList (), providedMetaData, null, virtualizerAspectId );
            this.model.addArtifact ( ai );
            this.modArtifacts.put ( ai.getId (), ai );

            if ( parent != null )
            {
                // add as child
                parent.getChildIds ().add ( ai.getId () );
                updateArtifact ( parentId, parent );
            }

            // refresh number of artifacts

            this.state.setNumberOfArtifacts ( this.modArtifacts.size () );
            this.state.incrementNumberOfBytes ( ai.getSize () );

            return ai;
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to create artifact", e );
        }
    }

    private ArtifactInformation updateArtifact ( final String id, final ArtifactModel artifact )
    {
        final ArtifactInformation result = ArtifactModel.toInformation ( id, artifact );
        this.modArtifacts.put ( id, result );
        return result;
    }

    private boolean internalDeleteArtifact ( final String id ) throws IOException
    {
        ensureTransaction ();

        final boolean result = this.transaction.delete ( id );

        this.model.removeArtifact ( id );
        final ArtifactInformation ai = this.modArtifacts.remove ( id );

        if ( ai == null )
        {
            return result;
        }

        // remove children as well

        if ( ai.getChildIds () != null )
        {
            for ( final String childId : ai.getChildIds () )
            {
                internalDeleteArtifact ( childId );
            }
        }

        // remove from parent's child list

        if ( ai.getParentId () != null )
        {
            final ArtifactModel parent = this.model.getArtifacts ().get ( ai.getParentId () );
            if ( parent != null )
            {
                parent.getChildIds ().remove ( id );
                updateArtifact ( ai.getParentId (), parent );
            }
        }

        // refresh number of artifacts

        this.state.setNumberOfArtifacts ( this.modArtifacts.size () );
        this.state.incrementNumberOfBytes ( -ai.getSize () );

        return result;
    }

    @Override
    public ArtifactInformation deletePlainArtifact ( final String id )
    {
        try
        {
            final ArtifactInformation artifact = this.modArtifacts.get ( id );

            if ( artifact == null )
            {
                return null;
            }

            final ArtifactInformation result = internalDeleteArtifact ( id ) ? artifact : null;

            return result;
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to delete artifact", e );
        }
    }

    @Override
    public boolean deleteArtifact ( final String id )
    {
        testLocked ();
        ensureTransaction ();

        final ArtifactInformation artifact = this.modArtifacts.get ( id );
        if ( artifact == null )
        {
            return false;
        }

        if ( !artifact.is ( "stored" ) )
        {
            throw new IllegalStateException ( String.format ( "Unable to delete artifact '%s'. It is not 'stored'.", id ) );
        }

        final boolean result = this.aspectContext.deleteArtifacts ( Collections.singleton ( id ) );

        // no need to refresh

        return result;
    }

    @Override
    public boolean stream ( final String artifactId, final IOConsumer<InputStream> consumer ) throws IOException
    {
        if ( this.transaction != null )
        {
            // stream from transaction
            return this.transaction.stream ( artifactId, consumer );
        }
        else
        {
            // stream from store
            return this.store.stream ( artifactId, consumer );
        }
    }

    @Override
    public boolean streamCacheEntry ( final MetaKey key, final IOConsumer<CacheEntry> consumer ) throws IOException
    {
        final CacheEntryInformation entry = this.cacheEntries.get ( key );

        if ( entry == null )
        {
            return false;
        }

        if ( this.cacheTransaction != null )
        {
            // stream from transaction
            return this.cacheTransaction.stream ( key, stream -> {
                consumer.accept ( new CacheEntry ( entry, stream ) );
            } );
        }
        else
        {
            // stream from store
            return this.cacheStore.stream ( key, stream -> {
                consumer.accept ( new CacheEntry ( entry, stream ) );
            } );
        }
    }

    @Override
    public void clear ()
    {
        testLocked ();
        ensureTransaction ();
        ensureCacheTransaction ();

        final String[] keys = this.modArtifacts.keySet ().toArray ( new String[this.modArtifacts.size ()] );
        for ( final String art : keys )
        {
            try
            {
                internalDeleteArtifact ( art );
            }
            catch ( final IOException e )
            {
                throw new RuntimeException ( "Failed to delete artifact: " + art, e );
            }
        }

        // clear cache entries

        this.modCacheEntries.clear ();
        this.model.getCacheEntries ().clear ();

        Exceptions.wrapException ( () -> this.cacheTransaction.clear () );

        // clear extracted channel meta data

        if ( this.model.getExtractedMetaData () != null )
        {
            this.model.getExtractedMetaData ().clear ();
        }

        // clear meta data cache

        this.metaDataCache = null;

        // clear validation messages

        this.model.getValidationMessages ().clear ();
        this.state.setValidationMessages ( Collections.emptyList () );

        // refresh number of artifacts

        this.state.setNumberOfArtifacts ( 0L );
        this.state.setNumberOfBytes ( 0L );
    }

    @Override
    public void addAspects ( final Set<String> aspectIds )
    {
        testLocked ();

        this.aspectContext.addAspects ( aspectIds );
    }

    @Override
    public void removeAspects ( final Set<String> aspectIds )
    {
        testLocked ();

        this.aspectContext.removeAspects ( aspectIds );

        postAspectEvents ( aspectIds, "remove" );
    }

    @Override
    public void refreshAspects ( final Set<String> aspectIds )
    {
        testLocked ();

        this.aspectContext.refreshAspects ( aspectIds );

        postAspectEvents ( aspectIds, "refresh" );
    }

    @Override
    public AspectMapModel getAspectModel ()
    {
        return this.model.getAspects ();
    }

    protected ArtifactInformation modifyArtifact ( final String artifactId, final Consumer<ArtifactModel> modification )
    {
        final ArtifactModel art = this.model.getArtifacts ().get ( artifactId );

        if ( art == null )
        {
            throw new IllegalStateException ( String.format ( "Unable to find artifact '%s'", artifactId ) );
        }

        // perform modification

        modification.accept ( art );

        // update from the model

        return updateArtifact ( artifactId, art );
    }

    @Override
    public ArtifactInformation setExtractedMetaData ( final String artifactId, final Map<MetaKey, String> metaData )
    {
        return modifyArtifact ( artifactId, art -> {
            // set the extracted data
            art.setExtractedMetaData ( new HashMap<> ( metaData ) );
        } );
    }

    @Override
    public ArtifactInformation setValidationMessages ( final String artifactId, final List<ValidationMessage> messages )
    {
        return modifyArtifact ( artifactId, art -> {
            // set validation messages
            art.setValidationMessages ( messages.stream ().map ( ValidationMessageModel::fromMessage ).collect ( toList () ) );
        } );
    }

    @Override
    public void setExtractedMetaData ( final Map<MetaKey, String> metaData )
    {
        this.model.setExtractedMetaData ( new HashMap<> ( metaData ) );
        this.metaDataCache = null;
    }

    @Override
    public void setValidationMessages ( final List<ValidationMessage> messages )
    {
        this.model.setValidationMessages ( messages.stream ().map ( ValidationMessageModel::fromMessage ).collect ( toList () ) );
        this.state.setValidationMessages ( messages );
    }

    @Override
    public Collection<ValidationMessage> getValidationMessages ()
    {
        return Collections.unmodifiableCollection ( this.state.build ().getValidationMessages () );
    }

    @Override
    public void regenerate ( final String artifactId )
    {
        testLocked ();

        this.aspectContext.regenerate ( artifactId );
    }

    @Override
    public Map<MetaKey, String> getChannelProvidedMetaData ()
    {
        return Collections.unmodifiableMap ( this.model.getProvidedMetaData () );
    }

    @Override
    public Map<MetaKey, String> getProvidedMetaData ()
    {
        return getChannelProvidedMetaData ();
    }

    @Override
    public Map<MetaKey, String> getExtractedMetaData ()
    {
        return Collections.unmodifiableMap ( this.model.getExtractedMetaData () );
    }

    @Override
    public void createCacheEntry ( final MetaKey key, final String name, final String mimeType, final IOConsumer<OutputStream> creator ) throws IOException
    {
        ensureCacheTransaction ();

        final long size = this.cacheTransaction.put ( key, creator );

        final CacheEntryInformation entry = new CacheEntryInformation ( key, name, size, mimeType, Instant.now () );
        this.modCacheEntries.put ( key, entry );
        this.model.getCacheEntries ().put ( key, CacheEntryModel.fromInformation ( entry ) );
    }

    @Override
    public ChannelDetails getChannelDetails ()
    {
        final ChannelDetails result = new ChannelDetails ();
        result.setDescription ( this.model.getDescription () );
        return result;
    }

    protected void postAspectEvents ( final Set<String> aspectIds, final String operation )
    {
        final String channelId = getChannelId ();
        StorageManager.executeAfterPersist ( () -> {
            for ( final String aspectFactoryId : aspectIds )
            {
                postAspectEvent ( channelId, aspectFactoryId, operation );
            }
        } );
    }

    protected void postAspectEvent ( final String channelId, final String aspectId, final String operation )
    {
        final Map<String, Object> data = new HashMap<> ( 2 );
        data.put ( "operation", operation );
        data.put ( "aspectFactoryId", aspectId );
        this.eventAdmin.postEvent ( new Event ( String.format ( "drone/channel/%s/aspect", makeSafeTopic ( channelId ) ), data ) );
    }

    private static String makeSafeTopic ( final String aspectId )
    {
        return aspectId.replaceAll ( "[^a-zA-Z0-9_\\-]", "_" );
    }
}
