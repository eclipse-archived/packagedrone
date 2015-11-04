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
import java.util.List;
import java.util.SortedMap;

import org.eclipse.packagedrone.repo.aspect.cleanup.CleanupConfiguration;
import org.eclipse.packagedrone.repo.aspect.cleanup.CleanupTester;
import org.eclipse.packagedrone.repo.aspect.cleanup.ResultEntry;
import org.eclipse.packagedrone.repo.aspect.cleanup.ResultKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public class CleanupTesterImpl implements CleanupTester
{

    @Override
    public SortedMap<ResultKey, List<ResultEntry>> testCleanup ( final Collection<ArtifactInformation> artifacts, final CleanupConfiguration configuration )
    {
        return CleanupListener.process ( configuration, CleanupListener.aggregate ( configuration.getAggregator (), configuration.getSorter (), configuration.isOnlyRootArtifacts (), artifacts ) );
    }

}
