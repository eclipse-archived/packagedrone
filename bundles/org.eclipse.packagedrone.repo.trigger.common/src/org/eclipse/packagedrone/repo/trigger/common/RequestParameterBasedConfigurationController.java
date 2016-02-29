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

import javax.validation.Valid;

import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;

public interface RequestParameterBasedConfigurationController<T>
{
    @RequestMapping ( method = RequestMethod.POST )
    public default ModelAndView updateJson ( @RequestParameter ( "channelId" ) final String channelId, @RequestParameter ( "triggerId" ) final String triggerId, @RequestParameter (
            value = "processorId",
            required = false ) final String processorId, @Valid @RequestParameter ( "configuration" ) final T command, final BindingResult result)
    {
        return processUpdate ( channelId, triggerId, processorId, command, result );
    }

    public ModelAndView processUpdate ( final String channelId, final String triggerId, final String processorId, final T command, final BindingResult result );
}
