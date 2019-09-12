/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Walker Funk - Trident Systems Inc. - added system call to clear function to delete empty directories
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.apm;

import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.lang.Runtime;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation.Manipulator;
import org.eclipse.packagedrone.repo.channel.CacheEntry;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.channel.ChannelState.Builder;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.repo.channel.apm.aspect.AspectContextImpl;
import org.eclipse.packagedrone.repo.channel.apm.aspect.AspectableContext;
import org.eclipse.packagedrone.repo.channel.apm.internal.Activator;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore.Transaction;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore;
import org.eclipse.packagedrone.repo.channel.provider.ChannelOperationContext;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.utils.Exceptions;
import org.eclipse.packagedrone.utils.io.IOConsumer;

public class ModifyContextImpl implements ModifyContext, AspectableContext
{
    private static final String FACET_GENERATOR = "generator";

    private final String channelId;

    private final BlobStore store;

    private final CacheStore cacheStore;

    private final SortedMap<String, String> aspectStates;

    private final SortedMap<String, String> modAspectStates;

    private final Map<MetaKey, String> extractedMetadata;

    private final Map<MetaKey, String> modExtractedMetadata;

    private final Map<MetaKey, String> providedMetadata;

    private final Map<MetaKey, String> modProvidedMetadata;

    private final Map<String, ArtifactInformation> artifacts;

    private final Map<String, ArtifactInformation> modArtifacts;

    private final Map<String, ArtifactInformation> generatorArtifacts;

    private final Map<String, ArtifactInformation> modGeneratorArtifacts;

    private final Map<MetaKey, CacheEntryInformation> cacheEntries;

    private final Map<MetaKey, CacheEntryInformation> modCacheEntries;

    private final Builder state;

    private Transaction transaction;

    private CacheStore.Transaction cacheTransaction;

    private final AspectContextImpl aspectContext;

    private SortedMap<MetaKey, String> metaDataCache;

    /**
     * Create a new empty modification context
     *
     * @param localChannelId
     *            the local channel id
     * @param eventAdmin
     *            the event admin to post events
     * @param store
     *            the blob store
     * @param cacheStore
     *            the cache store
     */
    public ModifyContextImpl ( final String channelId, final BlobStore store, final CacheStore cacheStore )
    {
        Objects.requireNonNull ( channelId );
        Objects.requireNonNull ( store );
        Objects.requireNonNull ( cacheStore );

        this.channelId = channelId;

        this.store = store;
        this.cacheStore = cacheStore;

        this.state = new Builder ();

        // main collections

        this.modAspectStates = new TreeMap<> ();
        this.modCacheEntries = new HashMap<> ();
        this.modArtifacts = new HashMap<> ();
        this.modGeneratorArtifacts = new HashMap<> ();
        this.modExtractedMetadata = new HashMap<> ();
        this.modProvidedMetadata = new HashMap<> ();

        // create unmodifiable collections

        this.aspectStates = Collections.unmodifiableSortedMap ( this.modAspectStates );
        this.cacheEntries = Collections.unmodifiableMap ( this.modCacheEntries );
        this.artifacts = Collections.unmodifiableMap ( this.modArtifacts );
        this.generatorArtifacts = Collections.unmodifiableMap ( this.modGeneratorArtifacts );
        this.extractedMetadata = Collections.unmodifiableMap ( this.modExtractedMetadata );
        this.providedMetadata = Collections.unmodifiableMap ( this.modProvidedMetadata );

        // aspect context

        this.aspectContext = new AspectContextImpl ( this, Activator.getProcessor () );
    }

    public ModifyContextImpl ( final String channelId, final BlobStore store, final CacheStore cacheStore, final ChannelState state, final Map<String, String> aspectStates, final Map<String, ArtifactInformation> artifacts, final Map<MetaKey, CacheEntryInformation> cacheEntries, final Map<MetaKey, String> extractedMetaData, final Map<MetaKey, String> providedMetaData )
    {
        Objects.requireNonNull ( channelId );
        Objects.requireNonNull ( store );
        Objects.requireNonNull ( cacheStore );

        this.channelId = channelId;

        this.store = store;
        this.cacheStore = cacheStore;

        this.state = new Builder ( state );

        // main collections

        this.modAspectStates = new TreeMap<> ( aspectStates );
        this.modCacheEntries = new HashMap<> ( cacheEntries );
        this.modArtifacts = new HashMap<> ( artifacts );
        this.modGeneratorArtifacts = this.modArtifacts.values ().stream ().filter ( art -> art.is ( FACET_GENERATOR ) ).collect ( toMap ( ArtifactInformation::getId, a -> a ) );
        this.modExtractedMetadata = new HashMap<> ( extractedMetaData );
        this.modProvidedMetadata = new HashMap<> ( providedMetaData );

        // create unmodifiable collections

        this.aspectStates = Collections.unmodifiableSortedMap ( this.modAspectStates );
        this.cacheEntries = Collections.unmodifiableMap ( this.modCacheEntries );
        this.artifacts = Collections.unmodifiableMap ( this.modArtifacts );
        this.generatorArtifacts = Collections.unmodifiableMap ( this.modGeneratorArtifacts );
        this.extractedMetadata = Collections.unmodifiableMap ( this.modExtractedMetadata );
        this.providedMetadata = Collections.unmodifiableMap ( this.modProvidedMetadata );

        // aspect context

        this.aspectContext = new AspectContextImpl ( this, Activator.getProcessor () );
    }

    public ModifyContextImpl ( final ModifyContextImpl other )
    {
        Objects.requireNonNull ( other );

        this.channelId = other.channelId;

        this.store = other.store;
        this.cacheStore = other.cacheStore;

        this.state = new Builder ( other.state.build () );

        // main collections

        this.modAspectStates = new TreeMap<> ( other.aspectStates );
        this.modCacheEntries = new HashMap<> ( other.cacheEntries );
        this.modArtifacts = new HashMap<> ( other.artifacts );
        this.modGeneratorArtifacts = new HashMap<> ( other.generatorArtifacts );
        this.modExtractedMetadata = new HashMap<> ( other.extractedMetadata );
        this.modProvidedMetadata = new HashMap<> ( other.providedMetadata );

        // create unmodifiable collections

        this.aspectStates = Collections.unmodifiableSortedMap ( this.modAspectStates );
        this.cacheEntries = Collections.unmodifiableMap ( this.modCacheEntries );
        this.artifacts = Collections.unmodifiableMap ( this.modArtifacts );
        this.generatorArtifacts = Collections.unmodifiableMap ( this.modGeneratorArtifacts );
        this.extractedMetadata = Collections.unmodifiableMap ( this.modExtractedMetadata );
        this.providedMetadata = Collections.unmodifiableMap ( this.modProvidedMetadata );

        // aspect context

        this.aspectContext = new AspectContextImpl ( this, Activator.getProcessor () );
    }

    @Override
    public String getChannelId ()
    {
        return this.channelId;
    }

    @Override
    public ChannelState getState ()
    {
        return this.state.build (); // will only create a new instance when necessary
    }

    @Override
    public SortedMap<MetaKey, String> getMetaData ()
    {
        // TODO: check if this should be synchronized

        if ( this.metaDataCache == null )
        {
            final TreeMap<MetaKey, String> tmp = new TreeMap<> ();
            if ( this.modExtractedMetadata != null )
            {
                tmp.putAll ( this.modExtractedMetadata );
            }
            if ( this.modProvidedMetadata != null )
            {
                tmp.putAll ( this.modProvidedMetadata );
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
    public Map<String, ArtifactInformation> getGeneratorArtifacts ()
    {
        return this.generatorArtifacts;
    }

    @Override
    public Map<MetaKey, CacheEntryInformation> getCacheEntries ()
    {
        return this.cacheEntries;
    }

    @Override
    public SortedMap<String, String> getAspectStates ()
    {
        return this.aspectStates;
    }

    @Override
    public SortedMap<String, String> getModifiableAspectStates ()
    {
        return this.modAspectStates;
    }

    @Override
    public void applyMetaData ( final Map<MetaKey, String> changes )
    {
        Objects.requireNonNull ( changes );

        testLocked ();

        for ( final Map.Entry<MetaKey, String> entry : changes.entrySet () )
        {
            final MetaKey key = entry.getKey ();
            final String value = entry.getValue ();

            if ( value == null )
            {
                this.modProvidedMetadata.remove ( key );
            }
            else
            {
                this.modProvidedMetadata.put ( key, value );
            }
        }

        // clear cache

        this.metaDataCache = null;

        // mark modified

        markModified ();

        // re-create all virtualized artifacts of the modified namespaces

        final Set<String> namespaces = changes.keySet ().stream ().map ( MetaKey::getNamespace ).collect ( Collectors.toSet () );

        // refresh and re-aggregate

        this.aspectContext.refreshAspects ( namespaces );
    }

    @Override
    public void applyMetaData ( final String artifactId, final Map<MetaKey, String> changes )
    {
        Objects.requireNonNull ( artifactId );
        Objects.requireNonNull ( changes );

        testLocked ();

        final ArtifactInformation artifact = this.modArtifacts.get ( artifactId );
        if ( artifact == null )
        {
            throw new IllegalStateException ( String.format ( "Artifact '%s' is unknown", artifactId ) );
        }

        if ( !artifact.getFacets ().contains ( "stored" ) )
        {
            throw new IllegalStateException ( String.format ( "Artifact '%s' is not 'stored'", artifactId ) );
        }

        final Manipulator m = artifact.createManipulator ();

        for ( final Map.Entry<MetaKey, String> entry : changes.entrySet () )
        {
            final MetaKey key = entry.getKey ();
            final String value = entry.getValue ();
            if ( value == null )
            {
                m.getProvidedMetaData ().remove ( key );
            }
            else
            {
                m.getProvidedMetaData ().put ( key, value );
            }
        }

        // update

        updateArtifact ( m );

        // mark modified

        markModified ();

        // regenerate generators

        if ( artifact.getFacets ().contains ( FACET_GENERATOR ) )
        {
            this.aspectContext.regenerate ( artifactId );
        }

        // TODO: new behavior - regenerate normal artifacts since this might have changed virtual artifacts
    }

    private void testLocked ()
    {
        if ( this.state.build ().isLocked () )
        {
            throw new IllegalStateException ( "Channel is locked" );
        }
    }

    @Override
    public void lock ()
    {
        this.state.setLocked ( true );
    }

    @Override
    public void unlock ()
    {
        this.state.setLocked ( false );
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
    public ArtifactInformation createArtifact ( final String parentId, final InputStream source, final String name, final Map<MetaKey, String> providedMetaData )
    {
        Objects.requireNonNull ( source );
        Objects.requireNonNull ( name );

        testLocked ();

        markModified ();

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
        Objects.requireNonNull ( name );
        Objects.requireNonNull ( generatorId );

        final InputStream finalSource;
        if ( source != null )
        {
            finalSource = source;
        }
        else
        {
            // provide an empty input stream
            finalSource = new ByteArrayInputStream ( new byte[0] );
        }

        testLocked ();

        markModified ();

        return Exceptions.wrapException ( () -> this.aspectContext.createGeneratorArtifact ( generatorId, finalSource, name, providedMetaData ) );
    }

    private class ArtifactAdditionImpl implements ArtifactAddition
    {
        private final String id;

        private final String parentId;

        private final String name;

        private final Instant creationTimestamp;

        private final Map<MetaKey, String> providedMetaData;

        private final Set<String> facets;

        private final String virtualizerAspectId;

        private boolean created = false;

        public ArtifactAdditionImpl ( final String id, final String parentId, final String name, final Instant creationTimestamp, final Map<MetaKey, String> providedMetaData, final Set<String> facets, final String virtualizerAspectId )
        {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.creationTimestamp = creationTimestamp;
            this.providedMetaData = providedMetaData != null ? new HashMap<> ( providedMetaData ) : Collections.emptyMap ();
            this.facets = facets != null ? new HashSet<> ( facets ) : Collections.emptySet ();
            this.virtualizerAspectId = virtualizerAspectId;
        }

        @Override
        public String getId ()
        {
            return this.id;
        }

        @Override
        public String getName ()
        {
            return this.name;
        }

        @Override
        public Instant getCreationTimestamp ()
        {
            return this.creationTimestamp;
        }

        @Override
        public ArtifactInformation create ( final InputStream stream )
        {
            if ( this.created )
            {
                throw new IllegalStateException ();
            }

            this.created = true;

            return handleCreatePlainArtifact ( this.parentId, stream, this.name, this.providedMetaData, this.facets, this.virtualizerAspectId );
        }
    }

    @Override
    public ArtifactAddition preparePlainArtifact ( final String parentArtifactId, final String name, final Map<MetaKey, String> providedMetaData, final Set<String> facets, final String virtualizerAspectId )
    {
        return new ArtifactAdditionImpl ( UUID.randomUUID ().toString (), parentArtifactId, name, Instant.now (), providedMetaData, facets, virtualizerAspectId );
    }

    public ArtifactInformation handleCreatePlainArtifact ( final String parentId, final InputStream source, final String name, final Map<MetaKey, String> providedMetaData, final Set<String> facets, final String virtualizerAspectId )
    {
        Objects.requireNonNull ( name );
        Objects.requireNonNull ( source );

        ensureTransaction ();

        markModified ();

        final String id = UUID.randomUUID ().toString ();

        try
        {
            final long size = this.transaction.create ( id, source );

            final ArtifactInformation parent;

            // validate parent

            if ( parentId != null )
            {
                parent = this.artifacts.get ( parentId );
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
            this.modArtifacts.put ( ai.getId (), ai );

            if ( ai.is ( FACET_GENERATOR ) )
            {
                this.modGeneratorArtifacts.put ( ai.getId (), ai );
            }

            if ( parent != null )
            {
                // add as child

                final Manipulator m = parent.createManipulator ();
                m.getChildIds ().add ( ai.getId () );
                updateArtifact ( m );
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

    private ArtifactInformation updateArtifact ( final Manipulator manipulator )
    {
        markModified ();

        final ArtifactInformation art = manipulator.build ();
        this.modArtifacts.put ( art.getId (), art );
        return art;
    }

    private boolean internalDeleteArtifact ( final String id ) throws IOException
    {
        ensureTransaction ();

        final boolean result = this.transaction.delete ( id );

        final ArtifactInformation ai = this.modArtifacts.remove ( id );

        if ( ai == null )
        {
            return result;
        }

        // remove from generators

        this.modGeneratorArtifacts.remove ( id );

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
            final ArtifactInformation parent = this.modArtifacts.get ( ai.getParentId () );
            if ( parent != null )
            {
                final Manipulator m = parent.createManipulator ();
                m.getChildIds ().remove ( id );
                updateArtifact ( m );
            }
        }

        // remove from generators

        if ( ai.is ( FACET_GENERATOR ) )
        {
            this.modGeneratorArtifacts.remove ( id );
        }

        // refresh number of artifacts

        this.state.setNumberOfArtifacts ( this.modArtifacts.size () );
        this.state.incrementNumberOfBytes ( -ai.getSize () );

        return result;
    }

    @Override
    public ArtifactInformation deletePlainArtifact ( final String id )
    {
        Objects.requireNonNull ( id );

        markModified ();

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
    public int deleteArtifacts ( final Set<String> ids )
    {
        Objects.requireNonNull ( ids );

        if ( ids.isEmpty () )
        {
            // shortcut
            return 0;
        }

        testLocked ();

        markModified ();

        ensureTransaction ();

        // check first

        for ( final String id : ids )
        {
            final ArtifactInformation artifact = this.modArtifacts.get ( id );
            if ( artifact == null )
            {
                continue;
            }

            if ( !artifact.is ( "stored" ) )
            {
                throw new IllegalStateException ( String.format ( "Unable to delete artifact '%s'. It is not 'stored'.", id ) );
            }
        }

        // now delete

        final int result = this.aspectContext.deleteArtifacts ( ids );

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
        Objects.requireNonNull ( key );
        Objects.requireNonNull ( consumer );

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

        markModified ();

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

        // clear generators

        this.modGeneratorArtifacts.clear ();

        // clear cache entries

        this.modCacheEntries.clear ();

        Exceptions.wrapException ( () -> this.cacheTransaction.clear () );

        // clear extracted channel meta data

        this.modExtractedMetadata.clear ();

        // clear meta data cache

        this.metaDataCache = null;

        // clear validation messages

        this.state.setValidationMessages ( Collections.emptyList () );

        // refresh number of artifacts

        this.state.setNumberOfArtifacts ( 0L );
        this.state.setNumberOfBytes ( 0L );

        final String cmd = "rm -rf " + System.getProperty ( "drone.storage.base" ) + "/channels/" + this.channelId + "/blobs/data/*";
        try
        {
            Process p = Runtime.getRuntime().exec( new String[] {"sh", "-c", cmd} );
        }
        catch ( Exception e )
        {
            throw new RuntimeException ( "Could not remove channel " + this.channelId + " data directories" );
        }
    }

    @Override
    public void addAspects ( final Set<String> aspectIds )
    {
        Objects.requireNonNull ( aspectIds, "'aspectIds' must not be null" );

        testLocked ();

        markModified ();

        this.aspectContext.addAspects ( aspectIds );
    }

    @Override
    public void removeAspects ( final Set<String> aspectIds )
    {
        Objects.requireNonNull ( aspectIds, "'aspectIds' must not be null" );

        testLocked ();

        markModified ();

        this.aspectContext.removeAspects ( aspectIds );

        postAspectEvents ( aspectIds, "remove" );
    }

    @Override
    public void refreshAspects ( Set<String> aspectIds )
    {
        testLocked ();

        markModified ();

        if ( aspectIds == null || aspectIds.isEmpty () )
        {
            aspectIds = new HashSet<> ( this.aspectStates.keySet () );
        }

        this.aspectContext.refreshAspects ( aspectIds );

        postAspectEvents ( aspectIds, "refresh" );
    }

    protected ArtifactInformation modifyArtifact ( final String artifactId, final Consumer<Manipulator> modification )
    {
        Objects.requireNonNull ( artifactId );
        Objects.requireNonNull ( modification );

        final ArtifactInformation art = this.artifacts.get ( artifactId );

        if ( art == null )
        {
            throw new IllegalStateException ( String.format ( "Unable to find artifact '%s'", artifactId ) );
        }

        // perform modification

        final Manipulator m = art.createManipulator ();
        modification.accept ( m );

        // mark modified

        markModified ();

        // update from the model

        return updateArtifact ( m );
    }

    @Override
    public ArtifactInformation setExtractedMetaData ( final String artifactId, final Map<MetaKey, String> metaData )
    {
        Objects.requireNonNull ( artifactId );
        Objects.requireNonNull ( metaData );

        return modifyArtifact ( artifactId, art -> {
            // set the extracted data
            art.setExtractedMetaData ( new HashMap<> ( metaData ) );
        } );
    }

    @Override
    public ArtifactInformation setValidationMessages ( final String artifactId, final List<ValidationMessage> messages )
    {
        Objects.requireNonNull ( artifactId );
        Objects.requireNonNull ( messages );

        return modifyArtifact ( artifactId, art -> {
            // set validation messages
            art.setValidationMessages ( messages );
        } );
    }

    @Override
    public void setExtractedMetaData ( final Map<MetaKey, String> metaData )
    {
        Objects.requireNonNull ( metaData );

        this.modExtractedMetadata.clear ();
        this.modExtractedMetadata.putAll ( metaData );

        this.metaDataCache = null;
        markModified ();
    }

    @Override
    public void setValidationMessages ( final List<ValidationMessage> messages )
    {
        Objects.requireNonNull ( messages );

        this.state.setValidationMessages ( messages );
        markModified ();
    }

    @Override
    public Collection<ValidationMessage> getValidationMessages ()
    {
        return Collections.unmodifiableCollection ( this.state.build ().getValidationMessages () );
    }

    @Override
    public void regenerate ( final String artifactId )
    {
        Objects.requireNonNull ( artifactId );

        testLocked ();

        this.aspectContext.regenerate ( artifactId );
        markModified ();
    }

    @Override
    public Map<MetaKey, String> getChannelProvidedMetaData ()
    {
        return this.providedMetadata;
    }

    @Override
    public Map<MetaKey, String> getProvidedMetaData ()
    {
        return getChannelProvidedMetaData ();
    }

    @Override
    public Map<MetaKey, String> getExtractedMetaData ()
    {
        return this.extractedMetadata;
    }

    @Override
    public void createCacheEntry ( final MetaKey key, final String name, final String mimeType, final IOConsumer<OutputStream> creator ) throws IOException
    {
        Objects.requireNonNull ( key );
        Objects.requireNonNull ( name );
        Objects.requireNonNull ( creator );

        ensureCacheTransaction ();

        final long size = this.cacheTransaction.put ( key, creator );

        final CacheEntryInformation entry = new CacheEntryInformation ( key, name, size, mimeType, Instant.now () );
        this.modCacheEntries.put ( key, entry );

        markModified ();
    }

    protected ChannelOperationContext getOperationContext ()
    {
        return ChannelContextAccessor.current ().orElseThrow ( () -> new IllegalStateException ( "No ChannelOperationContext present" ) );
    }

    protected void postAspectEvents ( final Set<String> aspectIds, final String operation )
    {
        final String channelId = this.channelId;
        StorageManager.executeAfterPersist ( () -> {
            for ( final String aspectFactoryId : aspectIds )
            {
                postAspectEvent ( channelId, aspectFactoryId, operation );
            }
        } );
    }

    protected void postAspectEvent ( final String channelId, final String aspectId, final String operation )
    {
        getOperationContext ().postAspectOperation ( aspectId, operation );
    }

    private void markModified ()
    {
        final Instant now = Instant.now ();
        this.state.setModificationTimestamp ( now );
    }
}
