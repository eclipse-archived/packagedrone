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

import java.util.Collections;
import java.util.SortedMap;

import org.eclipse.packagedrone.repo.MetaKey;

public class ChannelInformation extends ChannelId
{
    private final ChannelState state;

    private final SortedMap<MetaKey, String> metaData;

    private final SortedMap<String, String> aspectStates;

    public ChannelInformation ( final ChannelId id, final ChannelState state, final SortedMap<MetaKey, String> metaData, final SortedMap<String, String> aspectStates )
    {
        this ( id.getId (), id.getName (), state, metaData, aspectStates );
    }

    private ChannelInformation ( final String id, final String name, final ChannelState state, final SortedMap<MetaKey, String> metaData, final SortedMap<String, String> aspectStates )
    {
        super ( id, name );

        this.state = state;
        this.metaData = Collections.unmodifiableSortedMap ( metaData );
        this.aspectStates = Collections.unmodifiableSortedMap ( aspectStates );
    }

    public ChannelState getState ()
    {
        return this.state;
    }

    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    public String getMetaData ( final MetaKey key )
    {
        return this.metaData.get ( key );
    }

    public String getMetaData ( final String namespace, final String key )
    {
        return getMetaData ( new MetaKey ( namespace, key ) );
    }

    public SortedMap<String, String> getAspectStates ()
    {
        return this.aspectStates;
    }

    public boolean hasAspect ( final String aspectId )
    {
        return this.aspectStates.containsKey ( aspectId );
    }
}
