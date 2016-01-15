/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.trigger.http;

import java.util.Map;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.trigger.Trigger;
import org.eclipse.packagedrone.repo.trigger.TriggerFactory;

public class HttpTriggerFactory implements TriggerFactory
{
    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @Override
    public Trigger create ( final String channelId, final Map<String, String> configuration )
    {
        final String alias = configuration.get ( "alias" );
        if ( alias == null || alias.trim ().isEmpty () )
        {
            throw new IllegalArgumentException ( "'alias' must not be null or empty" );
        }

        return new HttpTrigger ( alias, this.service, channelId );
    }
}
