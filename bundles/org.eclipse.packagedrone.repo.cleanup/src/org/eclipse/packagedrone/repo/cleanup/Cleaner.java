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
package org.eclipse.packagedrone.repo.cleanup;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public class Cleaner
{
    public static final class Result
    {
        private final SortedMap<ResultKey, List<ResultEntry>> entries;

        public Result ( final SortedMap<ResultKey, List<ResultEntry>> entries )
        {
            this.entries = entries;
        }

        /**
         * Get a set of deleted artifact IDs
         *
         * @return a set of artifact IDs which should be deleted
         */
        public Stream<String> deletedSetStream ()
        {
            return this.entries.values ().stream ().flatMap ( list -> list.stream () ).filter ( entry -> entry.getAction () == Action.DELETE ).map ( entry -> entry.getArtifact ().getId () );
        }

        public SortedMap<ResultKey, List<ResultEntry>> getEntries ()
        {
            return this.entries;
        }
    }

    private final Supplier<Collection<? extends ArtifactInformation>> artifactSupplier;

    private Aggregator aggregator;

    private Sorter sorter;

    private boolean rootOnly = true;

    private boolean requireAll = false;

    private int numberOfEntries = Integer.MAX_VALUE;

    public Cleaner ( final Supplier<Collection<? extends ArtifactInformation>> artifactSupplier )
    {
        this.artifactSupplier = artifactSupplier;
    }

    public void setAggregator ( final Aggregator aggregator )
    {
        this.aggregator = aggregator;
    }

    public Aggregator getAggregator ()
    {
        return this.aggregator;
    }

    public void setSorter ( final Sorter sorter )
    {
        this.sorter = sorter;
    }

    public Sorter getSorter ()
    {
        return this.sorter;
    }

    public void setRootOnly ( final boolean rootOnly )
    {
        this.rootOnly = rootOnly;
    }

    public boolean isRootOnly ()
    {
        return this.rootOnly;
    }

    public void setRequireAll ( final boolean requireAll )
    {
        this.requireAll = requireAll;
    }

    public boolean isRequireAll ()
    {
        return this.requireAll;
    }

    public void setNumberOfEntries ( final int numberOfEntries )
    {
        if ( numberOfEntries < 0 )
        {
            this.numberOfEntries = 0;
        }
        else
        {
            this.numberOfEntries = numberOfEntries;
        }
    }

    public int getNumberOfEntries ()
    {
        return this.numberOfEntries;
    }

    public Result compute ()
    {
        if ( this.aggregator == null )
        {
            throw new IllegalStateException ( "There is no aggregator set" );
        }

        if ( this.sorter == null )
        {
            throw new IllegalStateException ( "There is no sorter set" );
        }

        final Map<List<String>, LinkedList<ArtifactInformation>> aggregation = aggregate ();

        final SortedMap<ResultKey, List<ResultEntry>> result = process ( aggregation );

        return new Result ( result );
    }

    private Map<List<String>, LinkedList<ArtifactInformation>> aggregate ()
    {
        final Map<List<String>, LinkedList<ArtifactInformation>> result = new HashMap<> ();

        for ( final ArtifactInformation art : this.artifactSupplier.get () )
        {
            if ( !art.is ( "stored" ) )
            {
                // we delete only stored artifacts
                continue;
            }

            if ( this.rootOnly && art.getParentId () != null )
            {
                // ignore non-root artifacts
                continue;
            }

            // make key
            final List<String> key = this.aggregator.makeKey ( art.getMetaData (), this.requireAll );

            if ( key == null )
            {
                // unable to build the key
                continue;
            }

            // get list
            LinkedList<ArtifactInformation> list = result.get ( key );

            // ... or create and put
            if ( list == null )
            {
                list = new LinkedList<> ();
                result.put ( key, list );
            }

            // add entry
            list.add ( art );
        }

        // sort by fields

        final Comparator<ArtifactInformation> comparator = this.sorter.makeComparator ();
        for ( final LinkedList<ArtifactInformation> list : result.values () )
        {
            Collections.sort ( list, comparator );
        }

        return result;
    }

    private SortedMap<ResultKey, List<ResultEntry>> process ( final Map<List<String>, LinkedList<ArtifactInformation>> aggregation )
    {
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

            final int cutOff = entry.getValue ().size () - this.numberOfEntries;
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
}
