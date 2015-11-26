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
package org.eclipse.packagedrone.repo.importer.aether.web;

import static org.eclipse.packagedrone.repo.importer.aether.AetherImporter.prepareDependencies;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.packagedrone.job.AbstractJsonJobFactory;
import org.eclipse.packagedrone.job.JobFactoryDescriptor;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.importer.aether.ImportConfiguration;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class AetherResolver extends AbstractJsonJobFactory<ImportConfiguration, AetherResult>
{
    private final static Logger logger = LoggerFactory.getLogger ( AetherResolver.class );

    public static final String ID = "org.eclipse.packagedrone.repo.importer.aether.web.resolver";

    private static final JobFactoryDescriptor DESCRIPTOR = new JobFactoryDescriptor () {

        @Override
        public LinkTarget getResultTarget ()
        {
            return null;
        }
    };

    private static final MetaKey KEY_GROUP_ID = new MetaKey ( "mvn", "groupId" );

    private static final MetaKey KEY_ARTIFACT_ID = new MetaKey ( "mvn", "artifactId" );

    private static final MetaKey KEY_CLASSIFIER = new MetaKey ( "mvn", "classifier" );

    private static final MetaKey KEY_EXTENSION = new MetaKey ( "mvn", "extension" );

    private static final MetaKey KEY_VERSION = new MetaKey ( "mvn", "version" );

    private ChannelService channelService;

    public AetherResolver ()
    {
        super ( ImportConfiguration.class );
    }

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    @Override
    protected String makeLabelFromData ( final ImportConfiguration data )
    {
        String label = "";

        if ( !data.getCoordinates ().isEmpty () )
        {
            label = data.getCoordinates ().get ( 0 ).toString ();
            if ( data.getCoordinates ().size () > 1 )
            {
                label += String.format ( " (and %s more)", data.getCoordinates ().size () - 1 );
            }
        }

        return String.format ( "Resolve maven dependencies: %s", label );
    }

    @Override
    public JobFactoryDescriptor getDescriptor ()
    {
        return DESCRIPTOR;
    }

    @Override
    protected AetherResult process ( final Context context, final ImportConfiguration cfg ) throws Exception
    {
        final Path tmpDir = Files.createTempDirectory ( "aether" );

        try
        {
            return markExisting ( prepareDependencies ( tmpDir, cfg ), cfg, this.channelService );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to test", e );
            throw e;
        }
        finally
        {
            Files.walkFileTree ( tmpDir, new RecursiveDeleteVisitor () );
            Files.deleteIfExists ( tmpDir );
        }
    }

    private static AetherResult markExisting ( final AetherResult result, final ImportConfiguration cfg, final ChannelService channelService )
    {
        final String channelId = cfg.getValidationChannelId ();
        if ( channelId == null || channelId.isEmpty () )
        {
            return result;
        }

        try
        {
            channelService.accessRun ( By.id ( cfg.getValidationChannelId () ), ReadableChannel.class, channel -> {
                markExisting ( result, channel );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            // silently ignore
        }
        return result;
    }

    private static void markExisting ( final AetherResult result, final ReadableChannel channel )
    {
        // build version map

        final Multimap<String, String> existing = HashMultimap.create ();

        for ( final ArtifactInformation ai : channel.getArtifacts () )
        {
            final String groupId = ai.getMetaData ().get ( KEY_GROUP_ID );
            final String artifactId = ai.getMetaData ().get ( KEY_ARTIFACT_ID );
            final String classifier = ai.getMetaData ().get ( KEY_CLASSIFIER );
            final String extension = ai.getMetaData ().get ( KEY_EXTENSION );

            final String version = ai.getMetaData ().get ( KEY_VERSION );

            if ( groupId != null && artifactId != null && version != null )
            {
                final String key = makeExistingKey ( groupId, artifactId, classifier, extension );
                existing.put ( key, version );
            }
        }

        // match

        for ( final AetherResult.Entry entry : result.getArtifacts () )
        {
            final String key = makeExistingKey ( entry.getCoordinates ().getGroupId (), entry.getCoordinates ().getArtifactId (), entry.getCoordinates ().getClassifier (), entry.getCoordinates ().getExtension () );
            entry.getExistingVersions ().addAll ( existing.get ( key ) );
        }
    }

    private static String makeExistingKey ( final String groupId, final String artifactId, final String classifier, final String extension )
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( groupId );
        sb.append ( ':' ).append ( artifactId );

        if ( classifier != null && !classifier.isEmpty () )
        {
            sb.append ( ':' ).append ( classifier );
        }

        if ( extension != null && !extension.isEmpty () )
        {
            sb.append ( ':' ).append ( extension );
        }

        return sb.toString ();
    }

}
