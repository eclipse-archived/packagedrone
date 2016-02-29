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
package org.eclipse.packagedrone.repo.trigger.cleanup;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.cleanup.Aggregator;
import org.eclipse.packagedrone.repo.cleanup.Cleaner;
import org.eclipse.packagedrone.repo.cleanup.Sorter;
import org.eclipse.packagedrone.repo.gson.MetaKeyTypeAdapter;
import org.eclipse.packagedrone.utils.converter.Stringify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Stringify
public class CleanupConfiguration
{
    private Aggregator aggregator;

    private Sorter sorter;

    private int numberOfEntries;

    private boolean rootOnly;

    private boolean ignoreWhenMissingFields;

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

    public void setNumberOfEntries ( final int numberOfEntries )
    {
        this.numberOfEntries = numberOfEntries;
    }

    public int getNumberOfEntries ()
    {
        return this.numberOfEntries;
    }

    public void setRootOnly ( final boolean rootOnly )
    {
        this.rootOnly = rootOnly;
    }

    public boolean isRootOnly ()
    {
        return this.rootOnly;
    }

    public void setIgnoreWhenMissingFields ( final boolean ignoreWhenMissingFields )
    {
        this.ignoreWhenMissingFields = ignoreWhenMissingFields;
    }

    public boolean isIgnoreWhenMissingFields ()
    {
        return this.ignoreWhenMissingFields;
    }

    private static Gson createGson ()
    {
        final GsonBuilder gb = new GsonBuilder ();

        gb.registerTypeAdapter ( MetaKey.class, MetaKeyTypeAdapter.INSTANCE );

        return gb.create ();
    }

    public String toJson ()
    {
        return createGson ().toJson ( this );
    }

    @Override
    public String toString ()
    {
        return toJson ();
    }

    public static CleanupConfiguration valueOf ( final String json )
    {
        return createGson ().fromJson ( json, CleanupConfiguration.class );
    }

    public void applyTo ( final Cleaner cleaner )
    {
        cleaner.setAggregator ( this.aggregator );
        cleaner.setSorter ( this.sorter );
        cleaner.setNumberOfEntries ( this.numberOfEntries );
        cleaner.setRootOnly ( true );
    }
}
