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
package org.eclipse.packagedrone.repo.aspect.cleanup.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.aspect.cleanup.Aggregator;
import org.eclipse.packagedrone.repo.aspect.cleanup.CleanupConfiguration;
import org.eclipse.packagedrone.repo.aspect.cleanup.ResultEntry;
import org.eclipse.packagedrone.repo.aspect.cleanup.ResultKey;
import org.eclipse.packagedrone.repo.aspect.cleanup.Sorter;
import org.eclipse.packagedrone.repo.aspect.cleanup.CleanupTester.Action;
import org.eclipse.packagedrone.repo.aspect.listener.ChannelListener;
import org.eclipse.packagedrone.repo.aspect.listener.PostAddContext;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupListener implements ChannelListener
{

    private final static Logger logger = LoggerFactory.getLogger ( CleanupListener.class );

    @Override
    public void artifactAdded ( final PostAddContext context ) throws Exception
    {
        final Map<MetaKey, String> metaData = context.getChannelMetaData ();

        final CleanupConfiguration cfg = MetaKeys.bind ( new CleanupConfiguration (), metaData );

        if ( cfg.getNumberOfVersions () <= 0 || cfg.getSorter () == null )
        {
            logger.info ( "Cleanup is unconfigured" );
            return;
        }

        final Map<List<String>, LinkedList<ArtifactInformation>> artifacts = aggregate ( cfg.getAggregator (), cfg.getSorter (), cfg.isOnlyRootArtifacts (), context.getChannelArtifacts () );

        final SortedMap<ResultKey, List<ResultEntry>> result = process ( cfg, artifacts );

        final Set<String> deleteSet = result.values ().stream ().flatMap ( list -> list.stream () ).filter ( entry -> entry.getAction () == Action.DELETE ).map ( entry -> entry.getArtifact ().getId () ).collect ( Collectors.toSet () );

        logger.debug ( "Deleting: {}", deleteSet );
        context.deleteArtifacts ( deleteSet );
    }

    static SortedMap<ResultKey, List<ResultEntry>> process ( final CleanupConfiguration configuration, final Map<List<String>, LinkedList<ArtifactInformation>> aggregation )
    {
        final int numVersions = configuration.getNumberOfVersions ();

        final SortedMap<ResultKey, List<ResultEntry>> result = new TreeMap<> ();

        for ( final Map.Entry<List<String>, LinkedList<ArtifactInformation>> entry : aggregation.entrySet () )
        {
            final ResultKey key = new ResultKey ( entry.getKey () );

            List<ResultEntry> value = result.get ( key );
            if ( value == null )
            {
                value = new LinkedList<> ();
                result.put ( key, value );
            }

            final int cutOff = entry.getValue ().size () - numVersions;
            int i = 0;
            for ( final ArtifactInformation art : entry.getValue () )
            {
                final Action action = i < cutOff ? Action.DELETE : Action.KEEP;
                value.add ( new ResultEntry ( art, action ) );
                i++;
            }
        }

        return result;
    }

    static Map<List<String>, LinkedList<ArtifactInformation>> aggregate ( final Aggregator aggregator, final Sorter sorter, final boolean rootOnly, final Collection<? extends ArtifactInformation> artifacts )
    {
        final Map<List<String>, LinkedList<ArtifactInformation>> result = new HashMap<> ();

        for ( final ArtifactInformation art : artifacts )
        {
            if ( !art.is ( "stored" ) )
            {
                continue;
            }

            if ( rootOnly && art.getParentId () != null )
            {
                // ignore non-root artifacts
                continue;
            }

            // make key
            final List<String> key = aggregator.makeKey ( art.getMetaData () );

            // get list
            LinkedList<ArtifactInformation> list = result.get ( key );

            // .. or create and put
            if ( list == null )
            {
                list = new LinkedList<> ();
                result.put ( key, list );
            }

            // add entry
            list.add ( art );
        }

        // sort by fields

        final Comparator<ArtifactInformation> comparator = sorter.makeComparator ();
        for ( final LinkedList<ArtifactInformation> list : result.values () )
        {
            Collections.sort ( list, comparator );
        }

        return result;
    }
}
