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
package org.eclipse.packagedrone.repo.trigger.common.unique;

import org.eclipse.packagedrone.repo.channel.AddingContext;
import org.eclipse.packagedrone.repo.trigger.Processor;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactory;
import org.eclipse.packagedrone.web.LinkTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniqueArtifactProcessorFactory implements ProcessorFactory
{
    static final Class<?>[] SUPPORTED_CONTEXTS = new Class<?>[] { AddingContext.class };

    private final static Logger logger = LoggerFactory.getLogger ( UniqueArtifactProcessorFactory.class );

    public static final String ID = "unique.artifact";

    @Override
    public Class<?>[] getSupportedContexts ()
    {
        return SUPPORTED_CONTEXTS;
    }

    @Override
    public Processor create ( final String configuration )
    {
        return new UniqueArtifactProcessor ( UniqueArtifactConfiguration.fromJson ( configuration ) );
    }

    @Override
    public String getLabel ()
    {
        return "Unique artifacts";
    }

    @Override
    public String getDescription ()
    {
        return "Ensure the uniqueness of artifacts.";
    }

    @Override
    public String getConfigurationUrl ()
    {
        try
        {
            return LinkTarget.createFromController ( UniqueArtifactConfigurationController.class, "configure" ).getUrl ();
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to generate configuration url", e );
            return null;
        }
    }

}
