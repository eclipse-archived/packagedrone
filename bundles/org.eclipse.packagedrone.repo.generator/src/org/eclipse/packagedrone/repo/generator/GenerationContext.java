/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.generator;

import java.util.Collection;

import org.eclipse.packagedrone.repo.channel.ArtifactContext;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.search.ArtifactLocator;
import org.eclipse.packagedrone.repo.channel.search.stream.StreamArtifactLocator;

public interface GenerationContext extends ArtifactContext
{
    public Collection<ArtifactInformation> getChannelArtifacts ();

    public default ArtifactLocator getArtifactLocator ()
    {
        return new StreamArtifactLocator ( () -> getChannelArtifacts ().stream () );
    }
}
