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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTrigger;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerHandler;
import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTrigger implements ConfiguredTrigger
{
    private final static Logger logger = LoggerFactory.getLogger ( HttpTrigger.class );

    private final HttpService httpService;

    private final SitePrefixService prefixService;

    private final HttpTriggerConfiguration configuration;

    private final ChannelService channelService;

    private boolean registered;

    public HttpTrigger ( final SitePrefixService prefixService, final HttpService httpService, final HttpTriggerConfiguration configuration, final ChannelService channelService )
    {
        this.prefixService = prefixService;
        this.httpService = httpService;
        this.configuration = configuration;
        this.channelService = channelService;
    }

    @Override
    public void start ( final ConfiguredTriggerHandler context ) throws Exception
    {
        final String alias = makeAlias ();
        logger.info ( "Registering trigger servlet: {}", alias );

        final Servlet servlet = new TriggerServlet ( () -> {
            runTrigger ( context );
        } );

        final Dictionary<String, Object> initparams = new Hashtable<> ();

        this.httpService.registerServlet ( alias, servlet, initparams, null );
        this.registered = true;
    }

    private void runTrigger ( final ConfiguredTriggerHandler context )
    {
        this.channelService.accessRun ( By.id ( context.getChannelId () ), ModifiableChannel.class, context::run );
    }

    private String makeAlias ()
    {
        return "/trigger/" + this.configuration.getEndpoint ();
    }

    @Override
    public TriggerDescriptor getState ()
    {
        return new TriggerDescriptor () {

            @Override
            public Class<?>[] getSupportedContexts ()
            {
                return new Class<?>[] { ModifiableChannel.class };
            }

            @Override
            public String getLabel ()
            {
                return HttpTriggerFactory.LABEL;
            }

            @Override
            public String getDescription ()
            {
                return HttpTriggerFactory.DESCRIPTION;
            }

            @Override
            public String getHtmlState ()
            {
                return renderHtmlState ();
            }
        };
    }

    protected String renderHtmlState ()
    {
        final StringWriter sw = new StringWriter ();
        final PrintWriter pw = new PrintWriter ( sw );

        final String prefix = this.prefixService.getSitePrefix ();
        pw.format ( "Run the trigger when a <code>POST</code> request is being made to: <a href=\"%1$s\">%1$s</a>", prefix + makeAlias () );

        return sw.toString ();
    }

    @Override
    public void stop ()
    {
        if ( this.registered )
        {
            final String alias = makeAlias ();
            logger.info ( "Un-registering trigger servlet: {}", alias );

            this.httpService.unregister ( alias );
            this.registered = false;
        }
    }
}
