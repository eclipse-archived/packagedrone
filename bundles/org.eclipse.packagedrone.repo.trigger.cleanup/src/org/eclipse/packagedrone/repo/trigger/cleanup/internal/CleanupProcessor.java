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
package org.eclipse.packagedrone.repo.trigger.cleanup.internal;

import static com.google.common.html.HtmlEscapers.htmlEscaper;

import java.io.PrintWriter;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.cleanup.Cleaner;
import org.eclipse.packagedrone.repo.cleanup.Cleaner.Result;
import org.eclipse.packagedrone.repo.cleanup.Field;
import org.eclipse.packagedrone.repo.trigger.Processor;
import org.eclipse.packagedrone.repo.trigger.cleanup.CleanupConfiguration;

public class CleanupProcessor implements Processor
{
    private final CleanupConfiguration cfg;

    public CleanupProcessor ( final CleanupConfiguration cfg )
    {
        this.cfg = cfg;
    }

    @Override
    public void process ( final Object context )
    {
        final ModifiableChannel channel = (ModifiableChannel)context;

        final Cleaner cleaner = new Cleaner ( channel::getArtifacts );

        this.cfg.applyTo ( cleaner );

        final Result result = cleaner.compute ();
        result.deletedSetStream ().forEach ( id -> channel.getContext ().deleteArtifact ( id ) );
    }

    @Override
    public void streamHtmlState ( final PrintWriter writer )
    {
        writer.append ( "Group all artifacts by: " );

        int i = 0;
        for ( final MetaKey field : this.cfg.getAggregator ().getFields () )
        {
            if ( i > 0 )
            {
                writer.append ( ", " );
            }
            writer.format ( "<code>%s</code>", htmlEscaper ().escape ( field.toString () ) );
            i++;
        }

        writer.append ( " then sort by the values of: " );

        i = 0;
        for ( final Field field : this.cfg.getSorter ().getFields () )
        {
            if ( i > 0 )
            {
                writer.append ( ", " );
            }
            writer.format ( "<code>%s</code> %s", htmlEscaper ().escape ( field.getKey ().toString () ), makeOrder ( field ) );
            i++;
        }

        writer.format ( " then delete all but the last <strong>%s</strong> entries of each group.", this.cfg.getNumberOfEntries () );

        if ( this.cfg.isRootOnly () )
        {
            writer.append ( " <strong>Only root</strong> artifacts will be processed." );
        }
        else
        {
            writer.append ( " <strong>Root and child</strong> artifacts will be processed." );
        }

        if ( this.cfg.isIgnoreWhenMissingFields () )
        {
            writer.append ( " Artifacts which are missing an aggregator field will be <strong>ignored</strong>." );
        }
        else
        {
            writer.append ( " <strong>All artifacts</strong> will be processed. Missing aggregator field values will be replaced by an empty string." );
        }
    }

    private String makeOrder ( final Field field )
    {
        switch ( field.getOrder () )
        {
            case ASCENDING:
                return "<span class=\"glyphicon glyphicon-sort-by-attributes\"></span>";
            case DESCENDING:
                return "<span class=\"glyphicon glyphicon-sort-by-attributes-alt\"></span>";
            default:
                return "";
        }
    }
}
