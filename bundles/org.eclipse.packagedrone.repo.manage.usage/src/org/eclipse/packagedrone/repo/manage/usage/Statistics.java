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
package org.eclipse.packagedrone.repo.manage.usage;

public class Statistics
{
    private long numberOfArtifacts;

    private long numberOfBytes;

    private String randomId;

    private String version;

    public long getNumberOfArtifacts ()
    {
        return this.numberOfArtifacts;
    }

    public long getNumberOfBytes ()
    {
        return this.numberOfBytes;
    }

    public String getRandomId ()
    {
        return this.randomId;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public void setNumberOfArtifacts ( final long numberOfArtifacts )
    {
        this.numberOfArtifacts = numberOfArtifacts;
    }

    public void setNumberOfBytes ( final long numberOfBytes )
    {
        this.numberOfBytes = numberOfBytes;
    }

    public void setRandomId ( final String randomId )
    {
        this.randomId = randomId;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }
}
