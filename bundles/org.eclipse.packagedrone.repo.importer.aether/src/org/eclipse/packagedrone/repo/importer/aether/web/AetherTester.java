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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.packagedrone.job.AbstractJsonJobFactory;
import org.eclipse.packagedrone.job.JobFactoryDescriptor;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.importer.aether.AetherImporter;
import org.eclipse.packagedrone.repo.importer.aether.Configuration;
import org.eclipse.packagedrone.repo.importer.aether.MavenCoordinates;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AetherTester extends AbstractJsonJobFactory<Configuration, AetherResult>
{

    private final static Logger logger = LoggerFactory.getLogger ( AetherTester.class );

    public static final String ID = "org.eclipse.packagedrone.repo.importer.aether.web.tester";

    private static final JobFactoryDescriptor DESCRIPTOR = new JobFactoryDescriptor () {

        @Override
        public LinkTarget getResultTarget ()
        {
            return null;
        }
    };

    public AetherTester ()
    {
        super ( Configuration.class );
    }

    @Override
    protected String makeLabelFromData ( final Configuration data )
    {
        return String.format ( "Test Maven import: %s", data.getCoordinates () );
    }

    @Override
    public JobFactoryDescriptor getDescriptor ()
    {
        return DESCRIPTOR;
    }

    @Override
    protected AetherResult process ( final Context context, final Configuration cfg ) throws Exception
    {
        final AetherResult result = new AetherResult ();

        final Path tmpDir = Files.createTempDirectory ( "aether" );

        try
        {
            final Collection<ArtifactResult> results = AetherImporter.process ( tmpDir, cfg );

            final ArtifactResult artRes = results.iterator ().next ();

            result.setResolved ( artRes.isResolved () );

            final ArtifactRepository repo = artRes.getRepository ();
            if ( repo instanceof RemoteRepository )
            {
                final RemoteRepository remRepo = (RemoteRepository)repo;
                result.setUrl ( remRepo.getUrl () );
            }

            if ( artRes.isResolved () )
            {
                result.setCoordinates ( MavenCoordinates.fromResult ( artRes ) );
            }
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

        return result;
    }

}
