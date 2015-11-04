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
package org.eclipse.packagedrone.repo.importer.aether;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.importer.ImportContext;
import org.eclipse.packagedrone.repo.importer.ImportSubContext;
import org.eclipse.packagedrone.repo.importer.Importer;
import org.eclipse.packagedrone.repo.importer.ImporterDescription;
import org.eclipse.packagedrone.repo.importer.SimpleImporterDescription;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;

import com.google.gson.GsonBuilder;

public class AetherImporter implements Importer
{

    public static final String ID = "aether";

    private static final SimpleImporterDescription DESCRIPTION = new SimpleImporterDescription ();

    static
    {
        DESCRIPTION.setId ( ID );
        DESCRIPTION.setLabel ( "Maven 2 Importer" );
        DESCRIPTION.setDescription ( "Import artifacts from Maven Repositories using Eclipse Aether" );
        DESCRIPTION.setStartTarget ( new LinkTarget ( "/import/{token}/aether/start" ) );
    }

    private final GsonBuilder gsonBuilder;

    public AetherImporter ()
    {
        this.gsonBuilder = new GsonBuilder ();
    }

    @Override
    public ImporterDescription getDescription ()
    {
        return DESCRIPTION;
    }

    @Override
    public void runImport ( final ImportContext context, final String configuration ) throws Exception
    {
        final Configuration cfg = this.gsonBuilder.create ().fromJson ( configuration, Configuration.class );
        runImport ( context, cfg );
    }

    private void runImport ( final ImportContext context, final Configuration cfg ) throws Exception
    {
        final Path tmpDir = Files.createTempDirectory ( "aether" );

        context.addCleanupTask ( () -> {
            Files.walkFileTree ( tmpDir, new RecursiveDeleteVisitor () );
            Files.deleteIfExists ( tmpDir );
        } );

        final Collection<ArtifactResult> results = process ( tmpDir, cfg );

        ImportSubContext main = null;

        for ( final ArtifactResult result : results )
        {
            if ( result.isResolved () )
            {
                final Artifact artifact = result.getArtifact ();

                final Map<MetaKey, String> metadata = makeMetaData ( artifact );

                if ( main == null )
                {
                    main = context.scheduleImport ( artifact.getFile ().toPath (), false, artifact.getFile ().getName (), metadata );
                }
                else
                {
                    main.scheduleImport ( artifact.getFile ().toPath (), false, artifact.getFile ().getName (), metadata );
                }
            }
        }
    }

    private static Map<MetaKey, String> makeMetaData ( final Artifact artifact )
    {
        final Map<MetaKey, String> md = new HashMap<> ();

        md.put ( new MetaKey ( "mvn", "groupId" ), artifact.getGroupId () );
        md.put ( new MetaKey ( "mvn", "artifactId" ), artifact.getArtifactId () );
        md.put ( new MetaKey ( "mvn", "version" ), artifact.getVersion () );
        md.put ( new MetaKey ( "mvn", "extension" ), artifact.getExtension () );
        if ( artifact.getClassifier () != null )
        {
            md.put ( new MetaKey ( "mvn", "classifier" ), artifact.getClassifier () );
        }

        return md;
    }

    public static Collection<ArtifactResult> process ( final Path tmpDir, final Configuration cfg ) throws ArtifactResolutionException
    {
        final RepositorySystem system = Helper.newRepositorySystem ();
        final RepositorySystemSession session = Helper.newRepositorySystemSession ( tmpDir, system );

        final List<RemoteRepository> repositories;
        if ( cfg.getUrl () == null || cfg.getUrl ().isEmpty () )
        {
            repositories = Arrays.asList ( Helper.newCentralRepository () );
        }
        else
        {
            repositories = Arrays.asList ( Helper.newRemoteRepository ( "drone.aether.import", cfg.getUrl () ) );
        }

        final Collection<ArtifactRequest> requests = new LinkedList<> ();

        // main artifact

        final DefaultArtifact artifact = new DefaultArtifact ( cfg.getCoordinates () );
        {
            final ArtifactRequest artifactRequest = new ArtifactRequest ();
            artifactRequest.setArtifact ( artifact );
            artifactRequest.setRepositories ( repositories );
            requests.add ( artifactRequest );
        }

        if ( cfg.isIncludeSources () )
        {
            // add source artifact

            final DefaultArtifact sourcesArtifacts = new DefaultArtifact ( artifact.getGroupId (), artifact.getArtifactId (), "sources", artifact.getExtension (), artifact.getVersion () );
            final ArtifactRequest artifactRequest = new ArtifactRequest ();
            artifactRequest.setArtifact ( sourcesArtifacts );
            artifactRequest.setRepositories ( repositories );
            requests.add ( artifactRequest );
        }

        // process

        return system.resolveArtifacts ( session, requests );
    }
}
