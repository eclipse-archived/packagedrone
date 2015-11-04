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
package org.eclipse.packagedrone.repo.channel;

public class ChannelArtifactInformation extends ArtifactInformation
{
    private final ChannelId channelId;

    public ChannelArtifactInformation ( final ChannelId channelId, final ArtifactInformation artifactInformation )
    {
        super ( artifactInformation );
        this.channelId = channelId;
    }

    public ChannelId getChannelId ()
    {
        return this.channelId;
    }

}
