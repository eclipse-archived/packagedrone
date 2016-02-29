/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.cleanup;

import org.eclipse.packagedrone.repo.MetaKeyBinding;
import org.eclipse.packagedrone.repo.aspect.cleanup.internal.CleanupAspect;
import org.eclipse.packagedrone.repo.cleanup.Aggregator;
import org.eclipse.packagedrone.repo.cleanup.Cleaner;
import org.eclipse.packagedrone.repo.cleanup.Sorter;

public class CleanupConfiguration
{
    @MetaKeyBinding ( namespace = CleanupAspect.ID, key = "number-of-versions" )
    private int numberOfVersions = 3;

    @MetaKeyBinding ( namespace = CleanupAspect.ID, key = "only-root" )
    private boolean onlyRootArtifacts;

    @MetaKeyBinding ( namespace = CleanupAspect.ID, key = "require-all-matching" )
    private boolean requireAllMatching;

    @MetaKeyBinding ( namespace = CleanupAspect.ID, key = "sorter" )
    private Sorter sorter;

    @MetaKeyBinding ( namespace = CleanupAspect.ID, key = "aggregator" )
    private Aggregator aggregator;

    public boolean isOnlyRootArtifacts ()
    {
        return this.onlyRootArtifacts;
    }

    public void setOnlyRootArtifacts ( final boolean onlyRootArtifacts )
    {
        this.onlyRootArtifacts = onlyRootArtifacts;
    }

    public boolean isRequireAllMatching ()
    {
        return this.requireAllMatching;
    }

    public void setRequireAllMatching ( final boolean requireAllMatching )
    {
        this.requireAllMatching = requireAllMatching;
    }

    public Aggregator getAggregator ()
    {
        return this.aggregator;
    }

    public void setAggregator ( final Aggregator aggregator )
    {
        this.aggregator = aggregator;
    }

    public void setSorter ( final Sorter sorter )
    {
        this.sorter = sorter;
    }

    public Sorter getSorter ()
    {
        return this.sorter;
    }

    public void setNumberOfVersions ( final int numberOfVersions )
    {
        this.numberOfVersions = numberOfVersions;
    }

    public int getNumberOfVersions ()
    {
        return this.numberOfVersions;
    }

    public void applyTo ( final Cleaner cleaner )
    {
        cleaner.setAggregator ( this.aggregator );
        cleaner.setSorter ( this.sorter );
        cleaner.setNumberOfEntries ( this.numberOfVersions );
        cleaner.setRequireAll ( this.requireAllMatching );
        cleaner.setRootOnly ( this.onlyRootArtifacts );
    }
}
