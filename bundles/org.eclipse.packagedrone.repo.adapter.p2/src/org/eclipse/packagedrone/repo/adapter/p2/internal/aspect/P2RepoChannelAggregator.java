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
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.aspect.aggregate.AggregationContext;
import org.eclipse.packagedrone.repo.aspect.aggregate.ChannelAggregator;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;

public class P2RepoChannelAggregator implements ChannelAggregator
{
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss.SSS" );

    @Override
    public Map<String, String> aggregateMetaData ( final AggregationContext context ) throws Exception
    {
        try ( Handle handle = Profile.start ( this, "aggregateMetaData" ) )
        {
            handle.task ( "Process : " + context.getArtifacts ().size () );

            final Map<String, String> result = new HashMap<> ();
            final ChannelStreamer streamer = new ChannelStreamer ( context.getChannelId (), context.getChannelMetaData (), true, true );

            Date lastTimestamp = null;
            for ( final ArtifactInformation ai : context.getArtifacts () )
            {
                final Date cts = ai.getCreationTimestamp ();

                if ( lastTimestamp == null || lastTimestamp.before ( cts ) )
                {
                    lastTimestamp = cts;
                }

                streamer.process ( ai, context::streamArtifact );
            }

            if ( lastTimestamp != null )
            {
                result.put ( "last-change", "" + lastTimestamp.getTime () );
                result.put ( "last-change-string", DATE_FORMAT.format ( lastTimestamp.getTime () ) );
            }

            // spool out to cache

            handle.task ( "Spool out" );

            streamer.spoolOut ( context::createCacheEntry );

            // perform validation

            handle.task ( "Validate" );

            final Map<String, Set<String>> duplicates = streamer.checkDuplicates ();
            for ( final Map.Entry<String, Set<String>> arts : duplicates.entrySet () )
            {
                context.validationError ( String.format ( "Installable units have the same ID (%s) but different checksums. This will cause an \"MD5 hash is not as expected\" error when working with P2.", arts.getKey () ), arts.getValue () );
            }

            return result;
        }
    }
}
