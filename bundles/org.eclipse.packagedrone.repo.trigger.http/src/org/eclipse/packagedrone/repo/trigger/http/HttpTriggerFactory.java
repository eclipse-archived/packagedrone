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
package org.eclipse.packagedrone.repo.trigger.http;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTrigger;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerFactory;
import org.eclipse.packagedrone.web.LinkTarget;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTriggerFactory implements ConfiguredTriggerFactory
{
    static final String DESCRIPTION = "This trigger will provide an HTTP endpoint which activates the trigger when getting called";

    static final String LABEL = "HTTP endpoint trigger";

    private final static Logger logger = LoggerFactory.getLogger ( HttpTriggerFactory.class );

    public static final String ID = "http.endpoint";

    private HttpService httpService;

    private SitePrefixService sitePrefixService;

    private ChannelService channelService;

    public void setHttpService ( final HttpService httpService )
    {
        this.httpService = httpService;
    }

    public void setSitePrefixService ( final SitePrefixService sitePrefixService )
    {
        this.sitePrefixService = sitePrefixService;
    }

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    @Override
    public ConfiguredTrigger create ( final String configuration )
    {
        return new HttpTrigger ( this.sitePrefixService, this.httpService, HttpTriggerConfiguration.fromJson ( configuration ), this.channelService );
    }

    @Override
    public String getLabel ()
    {
        return LABEL;
    }

    @Override
    public String getDescription ()
    {
        return DESCRIPTION;
    }

    @Override
    public String getConfigurationUrl ()
    {
        try
        {
            return LinkTarget.createFromController ( HttpTriggerController.class, "configure" ).getUrl ();
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to generate configuration url", e );
            return null;
        }
    }

}
