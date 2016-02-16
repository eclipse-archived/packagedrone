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
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;

public abstract class SimpleTriggerConfigurationController<T> extends AbstractCommonTriggerConfigurationController<T>
{
    private final String factoryId;

    private final String viewName;

    protected abstract T newModel ();

    protected abstract T parseModel ( String configuration );

    protected abstract String writeModel ( T configuration );

    public SimpleTriggerConfigurationController ( final String factoryId, final String viewName )
    {
        this.factoryId = factoryId;
        this.viewName = viewName;
    }

    @Override
    protected ModelAndView handleCreate ( final TriggeredChannel channel )
    {
        return new ModelAndView ( this.viewName, makeModel ( channel, null, this::newModel ) );
    }

    @Override
    protected ModelAndView handleEdit ( final TriggeredChannel channel, final TriggerHandle trigger )
    {
        return new ModelAndView ( this.viewName, makeModel ( channel, trigger, () -> parseModel ( trigger.getConfiguration ().get ().getConfiguration () ) ) );
    }

    @Override
    protected ModelAndView handleCreateUpdate ( final TriggeredChannel channel, final T command, final BindingResult result )
    {
        if ( result.hasErrors () )
        {
            return new ModelAndView ( this.viewName, makeModel ( channel, null, () -> command ) );
        }

        channel.addConfiguredTrigger ( this.factoryId, writeModel ( command ) );

        return new ModelAndView ( String.format ( "redirect:/trigger/channel/%s/list", urlPathSegmentEscaper ().escape ( channel.getId ().getId () ) ) );
    }

    @Override
    protected ModelAndView handleEditUpdate ( final TriggeredChannel channel, final TriggerHandle triggerHandle, final T command, final BindingResult result )
    {
        if ( result.hasErrors () )
        {
            return new ModelAndView ( this.viewName, makeModel ( channel, triggerHandle, () -> command ) );
        }

        channel.modifyConfiguredTrigger ( triggerHandle.getId (), writeModel ( command ) );

        return new ModelAndView ( String.format ( "redirect:/trigger/channel/%s/list", urlPathSegmentEscaper ().escape ( channel.getId ().getId () ) ) );
    }

}
