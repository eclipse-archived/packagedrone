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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.packagedrone.repo.ChannelAspectInformation;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.Severity;
import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.aspect.aggregate.AggregationContext;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.repo.aspect.listener.PostAddContext;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.repo.channel.apm.internal.Activator;
import org.eclipse.packagedrone.repo.event.AddedEvent;
import org.eclipse.packagedrone.repo.event.RemovedEvent;
import org.eclipse.packagedrone.utils.Exceptions;
import org.eclipse.packagedrone.utils.Holder;
import org.eclipse.packagedrone.utils.io.IOConsumer;
import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * Aspect capable channel context implementation
 * <p>
 * This class helps in implementing a modifiable channel which supports aspects.
 * </p>
 */
public class AspectContextImpl
{
    private final static Logger logger = LoggerFactory.getLogger ( AspectContextImpl.class );

    @FunctionalInterface
    public interface ArtifactCreator
    {
        public ArtifactInformation internalCreateArtifact ( final String parentId, final IOConsumer<OutputStream> stream, final String name, final Map<MetaKey, String> providedMetaData, final ArtifactType type, String virtualizerAspectId ) throws IOException;
    }

    private final AspectableContext context;

    private final ChannelAspectProcessor processor;

    private final SortedMap<String, String> aspectStates;

    private final Guard aggregation;

    private final Guard postAdd;

    private final RegenerationTracker tracker;

    public AspectContextImpl ( final AspectableContext context, final ChannelAspectProcessor processor )
    {
        this.context = context;
        this.processor = processor;
        this.aspectStates = context.getModifiableAspectStates ();

        this.aggregation = new Guard ( this::runAggregators );
        this.postAdd = new Guard ( this::runPostAdd );
        this.tracker = new RegenerationTracker ( this::runRegeneration );
    }

    public SortedMap<String, String> getAspectStates ()
    {
        return this.aspectStates;
    }

    protected void runPostAdd ()
    {
        logger.debug ( "Running post add" );
        Profile.run ( this, "runPostAdd", () -> {

            this.processor.process ( this.aspectStates.keySet (), ChannelAspect::getChannelListener, ( aspect, listener ) -> {

                logger.trace ( "\tRunning listener: {}", aspect.getId () );

                final PostAddContext ctx = new PostAddContext () {

                    @Override
                    public Collection<ArtifactInformation> getChannelArtifacts ()
                    {
                        return AspectContextImpl.this.context.getArtifacts ().values ();
                    }

                    @Override
                    public Map<MetaKey, String> getChannelMetaData ()
                    {
                        return AspectContextImpl.this.context.getChannelProvidedMetaData ();
                    }

                    @Override
                    public void deleteArtifacts ( final Set<String> artifactIds )
                    {
                        AspectContextImpl.this.deleteArtifacts ( artifactIds );
                    }
                };

                try
                {
                    listener.artifactAdded ( ctx );
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( "Failed to run post add listener", e );
                }

            } );

        } );
    }

    protected void runAggregators ()
    {
        logger.debug ( "Running aggregators" );

        Profile.run ( this, "runAggregators", () -> {

            final Map<MetaKey, String> metaData = new HashMap<> ();
            final List<ValidationMessage> messages = new CopyOnWriteArrayList<> ();

            this.processor.process ( this.aspectStates.keySet (), ChannelAspect::getChannelAggregator, ( aspect, aggregator ) -> {

                logger.trace ( "\tRunning aggregator: {}", aspect.getId () );

                final AggregationContext ctx = new AggregationContextImpl ( this.context, aspect.getId (), this.context.getChannelId (), this.context::getChannelDetails, messages::add );

                final Map<String, String> result = Exceptions.wrapException ( () -> aggregator.aggregateMetaData ( ctx ) );

                mergeNamespaceMetaData ( aspect, result, metaData );
            } );

            this.context.setExtractedMetaData ( metaData );
            this.context.setValidationMessages ( messages );
        } );
    }

    protected void runRegeneration ( final Set<String> artifactIds )
    {
        logger.debug ( "Running regeneration: {}", artifactIds );

        Profile.run ( this, "runRegeneration", () -> {

            for ( final String artifactId : artifactIds )
            {
                regenerate ( artifactId );
            }
        } );
    }

    public void addAspects ( final Set<String> aspectIds )
    {
        logger.debug ( "Adding aspects: {}", aspectIds );

        this.aggregation.guarded ( () -> {

            this.tracker.run ( () -> {

                // remove all virtualized

                removeVirtualized ();

                // add aspect information

                final Set<String> addedAspects = new HashSet<> ();
                for ( final ChannelAspectInformation aspect : this.processor.resolve ( aspectIds ) )
                {
                    final String versionString = aspect.getVersion () != null ? aspect.getVersion ().toString () : null;

                    if ( !this.aspectStates.containsKey ( aspect.getFactoryId () ) )
                    {
                        this.aspectStates.put ( aspect.getFactoryId (), versionString );
                        addedAspects.add ( aspect.getFactoryId () );
                    }
                }

                // run extractors and virtualizers at the same time

                for ( final ArtifactInformation art : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
                {
                    doStreamedRun ( art.getId (), path -> {

                        // run added extractors

                        final ArtifactInformation updatedArt = extractFor ( aspectIds, art, path );

                        // re-create all virtual
                        virtualize ( updatedArt, path, this.aspectStates.keySet () );
                    } );
                }

                // -> flush regeneration
            } );

            // -> aggregators run after with guard
        } );
    }

    public void removeAspects ( final Set<String> aspectIds )
    {
        logger.debug ( "Removing aspects: {}", aspectIds );

        try ( Handle handle = Profile.start ( this, "removeAspects" ) )
        {
            this.aggregation.guarded ( () -> {

                this.tracker.run ( () -> {

                    // remove all virtualized

                    removeVirtualized ();

                    // update model

                    for ( final String aspectId : aspectIds )
                    {
                        this.aspectStates.remove ( aspectId );
                    }

                    // remove selected extracted meta data

                    removeExtractedFor ( aspectIds );

                    // re-create all virtual

                    virtualizeFor ( this.aspectStates.keySet () );

                    // channel validation well updated when be re-generated

                    // -> flush regeneration
                } );

                // -> aggregators run after with guard
            } );

        }
    }

    private boolean filterValidation ( final Supplier<Collection<ValidationMessage>> input, final Consumer<List<ValidationMessage>> output, final Set<String> aspectIds )
    {
        final Collection<ValidationMessage> list = input.get ();

        final List<ValidationMessage> result = new CopyOnWriteArrayList<> ();

        boolean filtered = false;

        for ( final ValidationMessage msg : list )
        {
            if ( aspectIds.contains ( msg.getAspectId () ) )
            {
                filtered = true;
                continue;
            }

            result.add ( msg );
        }

        output.accept ( result );

        return filtered;
    }

    public void refreshAspects ( final Set<String> aspectIds )
    {
        final Set<String> effectiveAspectIds;
        if ( aspectIds == null || aspectIds.isEmpty () )
        {
            effectiveAspectIds = new HashSet<> ( this.aspectStates.keySet () );
        }
        else
        {
            effectiveAspectIds = aspectIds;
        }

        this.aggregation.guarded ( () -> {

            this.tracker.run ( () -> {

                // remove all virtualized

                removeVirtualized ();

                // update version map

                for ( final ChannelAspectInformation aspect : this.processor.resolve ( effectiveAspectIds ) )
                {
                    final String versionString = aspect.getVersion () != null ? aspect.getVersion ().toString () : null;
                    this.aspectStates.put ( aspect.getFactoryId (), versionString );
                }

                // run extractors and virtualizers at the same time

                for ( final ArtifactInformation art : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
                {
                    doStreamedRun ( art.getId (), path -> {

                        // run selected extractors

                        final ArtifactInformation updatedArt = extractFor ( aspectIds, art, path );

                        // re-create _all_ virtual

                        virtualize ( updatedArt, path, this.aspectStates.keySet () );

                    } );
                }

                // -> flush regeneration
            } );

            // -> aggregators run after with guard
        } );
    }

    public ArtifactInformation createArtifact ( final String parentId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData ) throws IOException
    {
        return this.aggregation.guarded ( () -> {

            return this.postAdd.guarded ( () -> {

                return this.tracker.run ( () -> {

                    return internalCreateArtifact ( parentId, stream, name, providedMetaData, ArtifactType.STORED, null );

                    // -> flush regeneration
                } );

                // -> run post add
            } );

            // --> aggregators run after guard
        } );
    }

    public ArtifactInformation createGeneratorArtifact ( final String generatorId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData ) throws IOException
    {
        return this.aggregation.guarded ( () -> {

            return this.postAdd.guarded ( () -> {

                return this.tracker.run ( () -> {

                    final ArtifactInformation result = internalCreateArtifact ( null, stream, name, providedMetaData, ArtifactType.GENERATOR, generatorId );

                    // run generator

                    generate ( result );

                    return result;

                    // --> flush regeneration
                } );

                // -> run post add
            } );

            // --> aggregators run after guard
        } );
    }

    public void regenerate ( final String artifactId )
    {
        this.aggregation.guarded ( () -> {

            this.tracker.run ( () -> {

                final ArtifactInformation artifact = this.context.getArtifacts ().get ( artifactId );

                if ( artifact == null )
                {
                    throw new IllegalStateException ( String.format ( "Unable to find artifact '%s'", artifactId ) );
                }

                deleteGenerated ( artifact );
                generate ( artifact );

                // -> flush regeneration

            } );

            // -> aggregators run after with guard
        } );
    }

    private void deleteGenerated ( final ArtifactInformation generator )
    {
        final Set<String> deletions = new HashSet<> ( 1 );

        for ( final String childId : generator.getChildIds () )
        {
            final ArtifactInformation child = this.context.getArtifacts ().get ( childId );
            if ( child == null )
            {
                continue;
            }

            if ( !child.is ( "generated" ) )
            {
                continue;
            }

            deletions.add ( childId );
        }

        deleteArtifacts ( deletions );
    }

    private void generate ( final ArtifactInformation artifact )
    {
        // run generator

        doStreamedRun ( artifact.getId (), file -> {
            final String generatorId = artifact.getVirtualizerAspectId ();
            final VirtualizerContextImpl ctx = new VirtualizerContextImpl ( generatorId, file, artifact, this.context, this::internalCreateArtifact, ArtifactType.GENERATED );

            Exceptions.wrapException ( () -> Activator.getGeneratorProcessor ().process ( artifact.getVirtualizerAspectId (), ctx ) );
        } );
    }

    private ArtifactInformation internalCreateArtifact ( final String parentId, final IOConsumer<OutputStream> producer, final String name, final Map<MetaKey, String> providedMetaData, final ArtifactType type, final String virtualizerAspectId ) throws IOException
    {
        final Path tmp = Files.createTempFile ( "upload-", null );

        return this.aggregation.guarded ( () -> {

            return this.tracker.run ( () -> {

                try
                {
                    // spool out to tmp file

                    try ( OutputStream out = new BufferedOutputStream ( Files.newOutputStream ( tmp ) ) )
                    {
                        producer.accept ( out );
                    }

                    // check veto

                    if ( checkVetoAdd ( name, tmp, type.isExternal () ) )
                    {
                        return null;
                    }

                    // store artifact

                    ArtifactInformation result;
                    try ( InputStream in = new BufferedInputStream ( Files.newInputStream ( tmp ) ) )
                    {
                        result = this.context.createPlainArtifact ( parentId, in, name, providedMetaData, type.getFacetTypes (), virtualizerAspectId );
                    }

                    // extract meta data

                    final ExtractionResult extraction = extractMetaData ( result, tmp, this.aspectStates.keySet () );
                    result = this.context.setExtractedMetaData ( result.getId (), extraction.metadata );
                    result = this.context.setValidationMessages ( result.getId (), extraction.messages );

                    fireArtifactCreated ( result );

                    // run virtualizers for artifact

                    virtualize ( result, tmp, this.aspectStates.keySet () );

                    // return result

                    return result;

                    // -> flush regeneration
                }
                finally
                {
                    Files.deleteIfExists ( tmp );
                }

            } );

            // -> aggregators run after with guard

        } );
    }

    private ArtifactInformation internalCreateArtifact ( final String parentId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData, final ArtifactType type, final String virtualizerAspectId ) throws IOException
    {
        return internalCreateArtifact ( parentId, out -> ByteStreams.copy ( stream, out ), name, providedMetaData, type, virtualizerAspectId );
    }

    private void fireArtifactCreated ( final ArtifactInformation artifact )
    {
        Profile.run ( this, "fireArtifactCreated", () -> {
            fireArtifactEvent ( artifact, new AddedEvent ( artifact.getId (), artifact.getMetaData () ) );
        } );
    }

    private void fireArtifactDeleted ( final ArtifactInformation artifact )
    {
        Profile.run ( this, "fireArtifactDeleted", () -> {
            fireArtifactEvent ( artifact, new RemovedEvent ( artifact.getId (), artifact.getMetaData () ) );
        } );
    }

    private void fireArtifactEvent ( final ArtifactInformation modifiedArtifact, final Object event )
    {
        logger.debug ( "fireArtifactEvent - artifact: {}, event: {}", modifiedArtifact, event );

        for ( final ArtifactInformation artifact : this.context.getGeneratorArtifacts ().values () )
        {
            logger.trace ( "\tTest artifact: {}", artifact );

            if ( this.tracker.isMarked ( artifact.getId () ) )
            {
                // already marked
                continue;
            }

            if ( artifact.equals ( modifiedArtifact ) )
            {
                logger.trace ( "\t\t-> is the modified one" );
                // we don't regenerate ourself
                continue;
            }

            if ( !artifact.is ( "generator" ) )
            {
                // and only true generators
                logger.trace ( "\t\t-> is not a generator" );
                continue;
            }

            Activator.getGeneratorProcessor ().process ( artifact.getVirtualizerAspectId (), generator -> {
                logger.trace ( "\t\t-> run 'shouldRegenerate'" );
                if ( generator.shouldRegenerate ( event ) )
                {
                    logger.trace ( "\t\t-> mark for regeneration" );
                    this.tracker.mark ( artifact.getId () );
                }
            } );
        }
    }

    private void virtualizeFor ( final Collection<String> aspects )
    {
        // we need to iterate over an array

        for ( final ArtifactInformation artifact : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
        {
            doStreamedRun ( artifact.getId (), tmp -> {
                virtualize ( artifact, tmp, aspects );
            } );
        }
    }

    private void removeVirtualized ()
    {
        final Set<String> artifacts = new HashSet<> ();

        for ( final ArtifactInformation artifact : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
        {
            if ( artifact.is ( "virtual" ) )
            {
                artifacts.add ( artifact.getId () );
            }
        }

        deleteArtifacts ( artifacts );
    }

    private void virtualize ( final ArtifactInformation artifact, final Path tmp, final Collection<String> aspects )
    {
        logger.debug ( "Running virtualize - {} ({})", artifact, aspects );

        this.processor.process ( aspects, ChannelAspect::getArtifactVirtualizer, ( aspect, virtualizer ) -> {
            final VirtualizerContextImpl ctx = new VirtualizerContextImpl ( aspect.getId (), tmp, artifact, this.context, this::internalCreateArtifact, ArtifactType.VIRTUAL );
            virtualizer.virtualize ( ctx );
        } );
    }

    private boolean checkVetoAdd ( final String name, final Path file, final boolean external )
    {
        final PreAddContextImpl ctx = new PreAddContextImpl ( name, file, external );

        this.processor.process ( this.aspectStates.keySet (), ChannelAspect::getChannelListener, listener -> {

            Exceptions.wrapException ( () -> listener.artifactPreAdd ( ctx ) );

        } );

        return ctx.isVeto ();
    }

    public boolean deleteArtifacts ( final Set<String> artifactIds )
    {
        return this.aggregation.guarded ( () -> {

            return this.tracker.run ( () -> {

                int count = 0;
                for ( final String artifactId : artifactIds )
                {
                    final boolean result = internalDeleteArtifact ( artifactId ) != null;
                    if ( result )
                    {
                        count++;
                    }
                }

                return count > 0;

                // -> flush regeneration

            } );

            // -> aggregators run after with guard

        } );
    }

    private ArtifactInformation internalDeleteArtifact ( final String artifactId )
    {
        final ArtifactInformation result = this.context.deletePlainArtifact ( artifactId );

        if ( result != null )
        {
            fireArtifactDeleted ( result );
        }

        return result;
    }

    private void removeNamespaces ( final Set<String> aspectIds, final Map<MetaKey, String> newMetaData )
    {
        final Iterator<Map.Entry<MetaKey, String>> i = newMetaData.entrySet ().iterator ();
        while ( i.hasNext () )
        {
            final Entry<MetaKey, String> entry = i.next ();
            if ( aspectIds.contains ( entry.getKey ().getNamespace () ) )
            {
                i.remove ();
            }
        }
    }

    private void removeExtractedFor ( final Set<String> aspectIds )
    {
        for ( final ArtifactInformation art : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
        {
            final Map<MetaKey, String> newMetaData = new HashMap<> ( art.getExtractedMetaData () );

            // remove all meta keys which we want to update

            removeNamespaces ( aspectIds, newMetaData );
            this.context.setExtractedMetaData ( art.getId (), newMetaData );

            // remove all the validation messages which we want to update

            final List<ValidationMessage> newValidationMessages = new LinkedList<> ();
            filterValidation ( art::getValidationMessages, newValidationMessages::addAll, aspectIds );

            this.context.setValidationMessages ( art.getId (), newValidationMessages );
        }
    }

    private ArtifactInformation extractFor ( final Set<String> aspectIds, final ArtifactInformation art, final Path path )
    {
        final ExtractionResult result = extractMetaData ( art, path, aspectIds );

        final Map<MetaKey, String> updatedMetaData = result.metadata;
        final Map<MetaKey, String> newMetaData = new HashMap<> ( art.getExtractedMetaData () );

        // remove all meta keys which we want to update

        removeNamespaces ( aspectIds, newMetaData );

        // insert new data

        newMetaData.putAll ( updatedMetaData );

        // remove all validation messages which we want to update

        final List<ValidationMessage> messages = new LinkedList<> ();
        filterValidation ( art::getValidationMessages, messages::addAll, aspectIds );

        // add validation messages

        messages.addAll ( result.messages );

        this.context.setExtractedMetaData ( art.getId (), newMetaData );
        return this.context.setValidationMessages ( art.getId (), messages );
    }

    private class ExtractionResult
    {
        final Map<MetaKey, String> metadata = new HashMap<> ();

        final List<ValidationMessage> messages = new LinkedList<> ();
    }

    private ExtractionResult extractMetaData ( final ArtifactInformation artifact, final Path path, final Collection<String> aspectIds )
    {
        final ExtractionResult result = new ExtractionResult ();
        this.processor.process ( aspectIds, ChannelAspect::getExtractor, ( aspect, extractor ) -> {

            Exceptions.wrapException ( () -> extractMetaData ( artifact, result.metadata, result.messages, path, aspect, extractor ) );

        } );
        return result;
    }

    private void extractMetaData ( final ArtifactInformation artifact, final Map<MetaKey, String> result, final List<ValidationMessage> messages, final Path path, final ChannelAspect aspect, final Extractor extractor ) throws Exception
    {
        final Map<String, String> md = new HashMap<> ();

        extractor.extractMetaData ( new Extractor.Context () {

            @Override
            public void validationMessage ( final Severity severity, final String message )
            {
                messages.add ( new ValidationMessage ( aspect.getId (), severity, message, Collections.singleton ( artifact.getId () ) ) );
            }

            @Override
            public String getName ()
            {
                return artifact.getName ();
            }

            @Override
            public Path getPath ()
            {
                return path;
            }

            @Override
            public Instant getCreationTimestamp ()
            {
                return artifact.getCreationInstant ();
            }
        }, md );

        // insert into metakey map

        mergeNamespaceMetaData ( aspect, md, result );
    }

    private void mergeNamespaceMetaData ( final ChannelAspect aspect, final Map<String, String> md, final Map<MetaKey, String> result )
    {
        if ( md == null )
        {
            return;
        }

        for ( final Map.Entry<String, String> entry : md.entrySet () )
        {
            result.put ( new MetaKey ( aspect.getId (), entry.getKey () ), entry.getValue () );
        }
    }

    private <T> T doStreamedCall ( final String artifactId, final Function<Path, T> operation )
    {
        try
        {
            final Holder<T> result = new Holder<> ();

            final boolean streamed = this.context.stream ( artifactId, stream -> {
                final Path tmp = Files.createTempFile ( "blob-", null );
                try
                {
                    try ( OutputStream os = new BufferedOutputStream ( Files.newOutputStream ( tmp ) ) )
                    {
                        ByteStreams.copy ( stream, os );
                    }

                    result.value = operation.apply ( tmp );
                }
                finally
                {
                    Files.deleteIfExists ( tmp );
                }
            } );

            if ( !streamed )
            {
                throw new IllegalStateException ( "Unable to stream blob for: " + artifactId );
            }

            return result.value;
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to stream blob", e );
        }
    }

    private void doStreamedRun ( final String artifactId, final Consumer<Path> consumer )
    {
        doStreamedCall ( artifactId, path -> {
            consumer.accept ( path );
            return null;
        } );
    }

    /**
     * Re-aggregate
     */
    public void aggregate ()
    {
        runAggregators ();
    }

}
