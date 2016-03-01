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
import org.eclipse.packagedrone.repo.trigger.Processor;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactory;
import org.eclipse.packagedrone.repo.trigger.cleanup.CleanupConfiguration;
import org.eclipse.packagedrone.web.LinkTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupProcessorFactory implements ProcessorFactory
{
    static final Class<?>[] SUPPORTED_CONTEXTS = new Class<?>[] { ModifiableChannel.class };

    private final static Logger logger = LoggerFactory.getLogger ( CleanupProcessorFactory.class );

    public static final String ID = "cleanup";

    @Override
    public Class<?>[] getSupportedContexts ()
    {
        return SUPPORTED_CONTEXTS;
    }

    @Override
    public Processor create ( final String configuration )
    {
        return new CleanupProcessor ( CleanupConfiguration.valueOf ( configuration ) );
    }

    @Override
    public String getLabel ()
    {
        return "Channel cleanup";
    }

    @Override
    public String getDescription ()
    {
        return "Delete artifacts from a channel by a defined aggregation and sort configuration";
    }

    @Override
    public String getConfigurationUrl ()
    {
        try
        {
            return LinkTarget.createFromController ( CleanupConfigurationController.class, "configure" ).getUrl ();
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to generate configuration url", e );
            return null;
        }
    }

}
