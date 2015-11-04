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
package org.eclipse.packagedrone.repo.channel.provider;

import java.util.Collection;

import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.IdTransformer;

public interface ChannelProvider
{
    public interface Listener
    {
        public void update ( Collection<? extends Channel> added, Collection<? extends Channel> removed );
    }

    public void addListener ( Listener listener );

    public void removeListener ( Listener listener );

    public Channel create ( ChannelDetails details, IdTransformer idTransformer );

    public ProviderInformation getInformation ();

    public default String getId ()
    {
        return getInformation ().getId ();
    }

    /**
     * Delete all channels which are currently provided by this provider
     */
    public void wipe ();
}
