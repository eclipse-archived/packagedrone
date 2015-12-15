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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.packagedrone.repo.MetaKey;

public interface ChannelProvider
{
    public void create ( @NonNull String channelId, @NonNull Map<MetaKey, String> configuration );

    public Channel load ( @NonNull String channelId, @NonNull Map<MetaKey, String> configuration );

    public ProviderInformation getInformation ();

    public default String getId ()
    {
        return getInformation ().getId ();
    }

}
