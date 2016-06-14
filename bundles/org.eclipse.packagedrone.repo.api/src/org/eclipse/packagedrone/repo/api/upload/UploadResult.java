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
package org.eclipse.packagedrone.repo.api.upload;

import java.util.ArrayList;
import java.util.List;

public class UploadResult
{
    private String channelId;

    private List<ArtifactInformation> createdArtifacts = new ArrayList<> ();

    private List<RejectedArtifact> rejectedArtifacts = new ArrayList<> ();

    public String getChannelId ()
    {
        return this.channelId;
    }

    public void setChannelId ( final String channelId )
    {
        this.channelId = channelId;
    }

    public List<ArtifactInformation> getCreatedArtifacts ()
    {
        return this.createdArtifacts;
    }

    public void setCreatedArtifacts ( final List<ArtifactInformation> artifacts )
    {
        this.createdArtifacts = artifacts;
    }

    public List<RejectedArtifact> getRejectedArtifacts ()
    {
        return this.rejectedArtifacts;
    }

    public void setRejectedArtifacts ( final List<RejectedArtifact> rejectedArtifacts )
    {
        this.rejectedArtifacts = rejectedArtifacts;
    }
}
