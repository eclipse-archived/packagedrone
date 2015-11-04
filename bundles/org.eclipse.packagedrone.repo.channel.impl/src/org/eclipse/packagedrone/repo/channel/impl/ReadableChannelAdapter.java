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
package org.eclipse.packagedrone.repo.channel.impl;

import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.provider.AccessContext;

public class ReadableChannelAdapter implements ReadableChannel
{
    private final AccessContext context;

    private final ChannelId descriptor;

    public ReadableChannelAdapter ( final ChannelId descriptor, final AccessContext context )
    {
        this.descriptor = descriptor;
        this.context = context;
    }

    @Override
    public ChannelId getId ()
    {
        return this.descriptor;
    }

    @Override
    public AccessContext getContext ()
    {
        return this.context;
    }
}
