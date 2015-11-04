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
package org.eclipse.packagedrone.repo.importer.job.internal;

import org.eclipse.packagedrone.job.AbstractJsonJobFactory;
import org.eclipse.packagedrone.job.JobFactoryDescriptor;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.importer.Importer;
import org.eclipse.packagedrone.repo.importer.job.ImporterJobConfiguration;
import org.eclipse.packagedrone.repo.importer.job.ImporterResult;
import org.eclipse.packagedrone.web.LinkTarget;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class ImporterJob extends AbstractJsonJobFactory<ImporterJobConfiguration, ImporterResult>
{
    private static final LinkTarget TARGET = new LinkTarget ( "/import/job/{id}/result" );

    private static final JobFactoryDescriptor DESCRIPTOR = new JobFactoryDescriptor () {

        @Override
        public LinkTarget getResultTarget ()
        {
            return TARGET;
        }
    };

    private ChannelService service;

    private final ServiceTracker<Importer, Importer> tracker;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    public ImporterJob ()
    {
        super ( ImporterJobConfiguration.class );

        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( Importer.class ).getBundleContext (), Importer.class, null );
    }

    public void start ()
    {
        this.tracker.open ();
    }

    public void stop ()
    {
        this.tracker.close ();
    }

    @Override
    protected ImporterResult process ( final Context context, final ImporterJobConfiguration cfg ) throws Exception
    {
        final Importer imp = getImporter ( cfg.getImporterId () );

        if ( imp == null )
        {
            throw new IllegalArgumentException ( String.format ( "Importer '%s' is unknown", cfg.getImporterId () ) );
        }

        final AbstractImportContext ctx;
        switch ( cfg.getDescriptor ().getType () )
        {
            case "channel":
                ctx = new ChannelImportContext ( this.service, cfg.getDescriptor ().getChannelId (), context );
                break;
            case "artifact":
                ctx = new ArtifactImportContext ( this.service, cfg.getDescriptor ().getChannelId (), cfg.getDescriptor ().getArtifactParentId (), context );
                break;
            default:
                throw new IllegalArgumentException ( String.format ( "Unknown import type: %s", cfg.getDescriptor ().getType () ) );
        }

        try
        {
            imp.runImport ( ctx, cfg.getConfiguration () );

            return ctx.process ();
        }
        finally
        {
            ctx.close ();
        }
    }

    private Importer getImporter ( final String importerId )
    {
        for ( final Importer importer : this.tracker.getTracked ().values () )
        {
            if ( importer.getDescription ().getId ().equals ( importerId ) )
            {
                return importer;
            }
        }

        return null;
    }

    @Override
    protected String makeLabelFromData ( final ImporterJobConfiguration data )
    {
        final Importer importer = getImporter ( data.getImporterId () );
        if ( importer != null )
        {
            return String.format ( "Import job - %s", importer.getDescription ().getLabel () );
        }
        else
        {
            return String.format ( "Import job: %s", data.getImporterId () );
        }
    }

    @Override
    public JobFactoryDescriptor getDescriptor ()
    {
        return DESCRIPTOR;
    }
}
