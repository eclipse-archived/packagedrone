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
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;
import org.eclipse.packagedrone.repo.web.utils.ChannelServiceController;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.common.CommonController;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.controller.form.FormData;

public abstract class AbstractCommonTriggerConfigurationController<T> extends ChannelServiceController
{
    protected abstract ModelAndView handleEditUpdate ( final TriggeredChannel channel, final TriggerHandle triggerHandle, final T command, final BindingResult result );

    protected abstract ModelAndView handleCreateUpdate ( final TriggeredChannel channel, final T command, final BindingResult result );

    protected abstract ModelAndView handleEdit ( final TriggeredChannel channel, final TriggerHandle triggerHandle );

    protected abstract ModelAndView handleCreate ( final TriggeredChannel channel );

    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView configure ( @RequestParameter ( "channelId" ) final String channelId, @RequestParameter (
            value = "triggerId", required = false ) final String triggerId)
    {
        return withChannel ( channelId, TriggeredChannel.class, channel -> {

            if ( triggerId == null || triggerId.isEmpty () )
            {
                return handleCreate ( channel );
            }

            final Optional<TriggerHandle> triggerHandle = channel.getTrigger ( triggerId );

            if ( !triggerHandle.isPresent () )
            {
                // either we have not trigger id or a valid one
                return CommonController.createNotFound ( "trigger", triggerId );
            }

            final TriggerHandle handle = triggerHandle.get ();
            if ( !handle.getConfiguration ().isPresent () )
            {
                // trigger is present but not configurable
                return notConfigurable ( channel, triggerId );
            }

            return handleEdit ( channel, handle );
        } );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView update ( @RequestParameter ( "channelId" ) final String channelId, @RequestParameter (
            value = "triggerId",
            required = false ) final String triggerId, @Valid @FormData ( "command" ) final T command, final BindingResult result)
    {
        return withChannel ( channelId, TriggeredChannel.class, channel -> {

            if ( triggerId == null || triggerId.isEmpty () )
            {
                return handleCreateUpdate ( channel, command, result );
            }

            final Optional<TriggerHandle> triggerHandle = channel.getTrigger ( triggerId );

            if ( !triggerHandle.isPresent () )
            {
                // either we have not trigger id or a valid one
                return CommonController.createNotFound ( "trigger", triggerId );
            }

            final TriggerHandle handle = triggerHandle.get ();
            if ( !handle.getConfiguration ().isPresent () )
            {
                // trigger is present but not configurable

                return notConfigurable ( channel, triggerId );
            }

            return handleEditUpdate ( channel, handle, command, result );
        } );
    }

    private ModelAndView notConfigurable ( final TriggeredChannel channel, final String triggerId )
    {
        return CommonController.createError ( "Error", "Error", String.format ( "Trigger '%s' is not configurable", triggerId ) );
    }

    protected Map<String, Object> makeModel ( final TriggeredChannel channel, final TriggerHandle trigger, final Supplier<T> configuration )
    {
        final Map<String, Object> result = new HashMap<> ();

        result.put ( "command", configuration.get () );
        result.put ( "channelId", channel.getId ().getId () );

        if ( trigger != null )
        {
            result.put ( "triggerId", trigger.getId () );
        }

        fillModel ( result );

        return result;
    }

    protected void fillModel ( final Map<String, Object> model )
    {
    }
}
