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
package org.eclipse.packagedrone.repo.channel.provider;

import org.eclipse.packagedrone.repo.channel.PreAddContext;

public interface ChannelOperationContext
{
    public ChannelOperationContext NOOP = new ChannelOperationContext () {};

    public default void postAspectOperation ( final String aspectId, final String operation )
    {
    }

    public default void artifactPreAdd ( final PreAddContext ctx )
    {
    }
}
