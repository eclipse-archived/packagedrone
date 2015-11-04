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
package org.eclipse.packagedrone.repo.channel.stats;

public class ChannelStatistics
{
    private long totalNumberOfArtifacts;

    private long totalNumberOfBytes;

    /**
     * Get the total number of artifacts
     * <p>
     * This number also includes virtual or generated artifacts
     * </p>
     *
     * @return the total number of artifacts
     */
    public long getTotalNumberOfArtifacts ()
    {
        return this.totalNumberOfArtifacts;
    }

    public void setTotalNumberOfArtifacts ( final long totalNumberOfArtifacts )
    {
        this.totalNumberOfArtifacts = totalNumberOfArtifacts;
    }

    /**
     * Get total number of stored bytes
     * <p>
     * The sum of all stored bytes. No matter what artifact type or storage
     * location.
     * </p>
     * 
     * @return the number of all stores bytes
     */
    public long getTotalNumberOfBytes ()
    {
        return this.totalNumberOfBytes;
    }

    public void setTotalNumberOfBytes ( final long totalNumberOfBytes )
    {
        this.totalNumberOfBytes = totalNumberOfBytes;
    }
}
