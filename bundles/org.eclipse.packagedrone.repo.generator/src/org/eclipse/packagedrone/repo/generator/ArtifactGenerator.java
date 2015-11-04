/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.generator;

import org.eclipse.packagedrone.repo.channel.ChannelArtifactInformation;
import org.eclipse.packagedrone.web.LinkTarget;

public interface ArtifactGenerator
{
    public static final String GENERATOR_ID_PROPERTY = "pm.generator.id";

    public void generate ( GenerationContext context ) throws Exception;

    public boolean shouldRegenerate ( Object event );

    public LinkTarget getAddTarget ();

    public default LinkTarget getEditTarget ( final ChannelArtifactInformation artifact )
    {
        return null;
    }
}
