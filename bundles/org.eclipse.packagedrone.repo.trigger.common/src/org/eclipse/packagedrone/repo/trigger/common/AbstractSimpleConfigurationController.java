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
package org.eclipse.packagedrone.repo.trigger.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.validation.Valid;

import org.eclipse.packagedrone.repo.trigger.TriggerHandle;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessor;
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;
import org.eclipse.packagedrone.repo.web.utils.ChannelServiceController;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.controller.form.FormData;

public abstract class AbstractSimpleConfigurationController<T> extends ChannelServiceController
{
    protected abstract ModelAndView handleEditUpdate ( final TriggeredChannel channel, final TriggerHandle triggerHandle, final TriggerProcessor triggerProcessor, final T command, final BindingResult result );

    protected abstract ModelAndView handleCreateUpdate ( final TriggeredChannel channel, final TriggerHandle triggerHandle, final T command, final BindingResult result );

    protected abstract ModelAndView handleEdit ( final TriggeredChannel channel, final TriggerHandle trigger, final TriggerProcessor processor );

    protected abstract ModelAndView handleCreate ( final TriggeredChannel channel, final TriggerHandle trigger );

    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView configure ( @RequestParameter ( "channelId" ) final String channelId, @RequestParameter ( "triggerId" ) final String triggerId, @RequestParameter (
            value = "processorId", required = false ) final String processorId)
    {
        return withChannel ( channelId, TriggeredChannel.class, channel -> {

            final Optional<TriggerHandle> trigger = channel.getTrigger ( triggerId );

            if ( !trigger.isPresent () )
            {
                return notFound ( "trigger", triggerId );
            }

            if ( processorId == null || processorId.isEmpty () )
            {
                // handle create
                return handleCreate ( channel, trigger.get () );
            }
            else
            {
                // handle edit
                final Optional<TriggerProcessor> processor = trigger.get ().getProcessor ( processorId );
                if ( !processor.isPresent () )
                {
                    return notFound ( "processor", processorId );
                }
                return handleEdit ( channel, trigger.get (), processor.get () );
            }
        } );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView update ( @RequestParameter ( "channelId" ) final String channelId, @RequestParameter ( "triggerId" ) final String triggerId, @RequestParameter (
            value = "processorId",
            required = false ) final String processorId, @Valid @FormData ( "command" ) final T command, final BindingResult result)
    {
        return withChannel ( channelId, TriggeredChannel.class, channel -> {

            final Optional<TriggerHandle> trigger = channel.getTrigger ( triggerId );

            if ( !trigger.isPresent () )
            {
                return notFound ( "trigger", triggerId );
            }

            if ( processorId == null || processorId.isEmpty () )
            {
                // handle create
                return handleCreateUpdate ( channel, trigger.get (), command, result );
            }
            else
            {
                // handle edit
                final Optional<TriggerProcessor> processor = trigger.get ().getProcessor ( processorId );
                if ( !processor.isPresent () )
                {
                    return notFound ( "processor", processorId );
                }
                return handleEditUpdate ( channel, trigger.get (), processor.get (), command, result );
            }
        } );
    }

    protected Map<String, Object> makeModel ( final TriggeredChannel channel, final TriggerHandle trigger, final TriggerProcessor processor, final Supplier<T> configuration )
    {
        final Map<String, Object> result = new HashMap<> ();

        result.put ( "command", configuration.get () );
        result.put ( "channelId", channel.getId ().getId () );
        result.put ( "triggerId", trigger.getId () );

        if ( processor != null )
        {
            result.put ( "processorId", processor.getId () );
        }

        return result;
    }

    protected ModelAndView notFound ( final String type, final String id )
    {
        final Map<String, Object> model = new HashMap<> ( 2 );

        model.put ( "type", type );
        model.put ( "id", id );

        return new ModelAndView ( "notFound", model );
    }

}
