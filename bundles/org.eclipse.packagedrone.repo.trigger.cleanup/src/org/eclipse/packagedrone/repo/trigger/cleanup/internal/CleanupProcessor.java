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

import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.cleanup.Cleaner;
import org.eclipse.packagedrone.repo.cleanup.Cleaner.Result;
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

}
