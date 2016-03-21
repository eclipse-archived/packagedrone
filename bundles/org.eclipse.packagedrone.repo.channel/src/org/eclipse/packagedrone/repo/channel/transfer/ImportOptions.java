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
package org.eclipse.packagedrone.repo.channel.transfer;

public class ImportOptions
{
    private boolean useNames;

    private boolean processTriggers;

    public boolean isUseNames ()
    {
        return this.useNames;
    }

    public void setUseNames ( final boolean useNames )
    {
        this.useNames = useNames;
    }

    public boolean isProcessTriggers ()
    {
        return this.processTriggers;
    }

    public void setProcessTriggers ( final boolean processTriggers )
    {
        this.processTriggers = processTriggers;
    }
}
