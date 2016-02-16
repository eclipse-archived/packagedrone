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
package org.eclipse.packagedrone.repo.channel.impl.trigger;

import java.util.function.Consumer;

import org.eclipse.packagedrone.repo.trigger.ConfiguredTrigger;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerFactory;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerHandler;
import org.eclipse.packagedrone.repo.trigger.TriggerConfiguration;
import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;
import org.eclipse.scada.utils.ExceptionHelper;

public class ConfiguredTriggerInstance
{
    private final Consumer<ConfiguredTriggerFactory> listener = this::setFactory;

    private final String channelId;

    private final ConfiguredTriggerFactoryTracker tracker;

    private TriggerConfiguration configuration;

    private final Consumer<Object> runner;

    private ConfiguredTrigger service;

    private Exception error;

    private ConfiguredTriggerFactory factory;

    public ConfiguredTriggerInstance ( final String channelId, final ConfiguredTriggerFactoryTracker tracker, final TriggerConfiguration configuration, final Consumer<Object> runner )
    {
        this.channelId = channelId;
        this.tracker = tracker;
        this.configuration = configuration;
        this.runner = runner;

        tracker.addListener ( configuration.getTriggerFactoryId (), this.listener );
    }

    public synchronized void update ( final String configuration )
    {
        disposeService ();
        this.configuration = new TriggerConfiguration ( this.configuration.getTriggerFactoryId (), configuration );
        tryCreate ();
    }

    public void dispose ()
    {
        this.tracker.removeListener ( this.configuration.getTriggerFactoryId (), this.listener );
        disposeService ();
    }

    protected synchronized void setFactory ( final ConfiguredTriggerFactory factory )
    {
        disposeService ();
        this.factory = factory;
        tryCreate ();
    }

    private void tryCreate ()
    {
        if ( this.factory != null )
        {
            try
            {
                this.error = null; // clear error first
                this.service = this.factory.create ( this.configuration.getConfiguration () );
                this.service.start ( makeHandler () );
            }
            catch ( final Exception e )
            {
                this.error = e;
            }
        }
    }

    private ConfiguredTriggerHandler makeHandler ()
    {
        // FIXME: run with Disposing wrapper

        return new ConfiguredTriggerHandler () {

            @Override
            public String getChannelId ()
            {
                return ConfiguredTriggerInstance.this.channelId;
            }

            @Override
            public void run ( final Object context )
            {
                ConfiguredTriggerInstance.this.runner.accept ( context );
            }
        };
    }

    private void disposeService ()
    {
        if ( this.service != null )
        {
            this.service.stop ();
            this.service = null;
            this.error = null;
        }
    }

    public TriggerDescriptor getState ()
    {
        final ConfiguredTriggerFactory factory = this.factory;
        final Throwable e = this.error;
        if ( factory != null && e != null )
        {
            return new TriggerDescriptor () {

                @Override
                public String getLabel ()
                {
                    return factory.getLabel ();
                }

                @Override
                public String getDescription ()
                {
                    return factory.getDescription ();
                }

                @Override
                public String getHtmlState ()
                {
                    final StringBuilder sb = new StringBuilder ();

                    sb.append ( "<div class=\"alert alert-danger\">" );
                    sb.append ( "<strong>Initialization failure!</strong> " );
                    sb.append ( "The trigger failed to initialize: " );
                    sb.append ( "<code>" ).append ( ExceptionHelper.getMessage ( e ) ).append ( "</code>" );
                    sb.append ( "</div>" );

                    return sb.toString ();
                }

                @Override
                public Class<?>[] getSupportedContexts ()
                {
                    return new Class<?>[] {};
                }
            };
        }

        final ConfiguredTrigger service = this.service;

        if ( service != null )
        {
            return service.getState ();
        }

        return null;
    }

}
