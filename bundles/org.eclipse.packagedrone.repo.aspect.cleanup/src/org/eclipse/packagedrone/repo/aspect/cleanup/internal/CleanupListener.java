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
package org.eclipse.packagedrone.repo.aspect.cleanup.internal;

import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.aspect.cleanup.CleanupConfiguration;
import org.eclipse.packagedrone.repo.aspect.listener.ChannelListener;
import org.eclipse.packagedrone.repo.aspect.listener.PostAddContext;
import org.eclipse.packagedrone.repo.cleanup.Cleaner;
import org.eclipse.packagedrone.repo.cleanup.Cleaner.Result;
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
            logger.info ( "Cleanup is not configured" );
            return;
        }

        final Cleaner cleaner = new Cleaner ( context::getChannelArtifacts );

        cfg.applyTo ( cleaner );

        final Result result = cleaner.compute ();

        final Set<String> deleteSet = result.deletedSetStream ().collect ( toSet () );

        logger.debug ( "Deleting: {}", deleteSet );
        context.deleteArtifacts ( deleteSet );
    }

}
