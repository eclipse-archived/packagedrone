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

import static org.eclipse.packagedrone.repo.importer.aether.AetherImporter.preparePlain;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.packagedrone.job.AbstractJsonJobFactory;
import org.eclipse.packagedrone.job.JobFactoryDescriptor;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.importer.aether.ImportConfiguration;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AetherTester extends AbstractJsonJobFactory<ImportConfiguration, AetherResult>
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
        super ( ImportConfiguration.class );
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

        return String.format ( "Test Maven import: %s", label );
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
            return preparePlain ( tmpDir, cfg );

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

}
