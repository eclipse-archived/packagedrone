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

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;

import org.eclipse.packagedrone.repo.trigger.TriggerHandle;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessor;
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;

public abstract class SimpleProcessorConfigurationController<T> extends AbstractCommonProcessorConfigurationController<T>
{
    private final String processorFactoryId;

    private final String viewName;

    protected abstract T newModel ();

    protected abstract T parseModel ( String configuration );

    protected abstract String writeModel ( T configuration );

    public SimpleProcessorConfigurationController ( final String processorFactoryId, final String viewName )
    {
        this.processorFactoryId = processorFactoryId;
        this.viewName = viewName;
    }

    @Override
    protected ModelAndView handleCreate ( final TriggeredChannel channel, final TriggerHandle trigger )
    {
        return new ModelAndView ( this.viewName, makeModel ( channel, trigger, null, this::newModel ) );
    }

    @Override
    protected ModelAndView handleEdit ( final TriggeredChannel channel, final TriggerHandle trigger, final TriggerProcessor processor )
    {
        return new ModelAndView ( this.viewName, makeModel ( channel, trigger, processor, () -> parseModel ( processor.getConfiguration ().getConfiguration () ) ) );
    }

    @Override
    protected ModelAndView handleCreateUpdate ( final TriggeredChannel channel, final TriggerHandle triggerHandle, final T command, final BindingResult result )
    {
        if ( result.hasErrors () )
        {
            return new ModelAndView ( this.viewName, makeModel ( channel, triggerHandle, null, () -> command ) );
        }

        channel.addProcessor ( triggerHandle.getId (), this.processorFactoryId, writeModel ( command ) );

        return new ModelAndView ( String.format ( "redirect:/trigger/channel/%s/list", urlPathSegmentEscaper ().escape ( channel.getId ().getId () ) ) );
    }

    @Override
    protected ModelAndView handleEditUpdate ( final TriggeredChannel channel, final TriggerHandle triggerHandle, final TriggerProcessor triggerProcessor, final T command, final BindingResult result )
    {
        if ( result.hasErrors () )
        {
            return new ModelAndView ( this.viewName, makeModel ( channel, triggerHandle, triggerProcessor, () -> command ) );
        }

        channel.modifyProcessor ( triggerHandle.getId (), triggerProcessor.getId (), writeModel ( command ) );

        return new ModelAndView ( String.format ( "redirect:/trigger/channel/%s/list", urlPathSegmentEscaper ().escape ( channel.getId ().getId () ) ) );
    }

}
