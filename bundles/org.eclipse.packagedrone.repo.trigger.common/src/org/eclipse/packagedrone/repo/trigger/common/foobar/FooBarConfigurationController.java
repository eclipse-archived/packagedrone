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
package org.eclipse.packagedrone.repo.trigger.common.foobar;

import javax.servlet.annotation.HttpConstraint;

import org.eclipse.packagedrone.repo.trigger.common.FormBasedConfigurationController;
import org.eclipse.packagedrone.repo.trigger.common.SimpleProcessorConfigurationController;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/trigger/processor.factory/foobar/configure" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class FooBarConfigurationController extends SimpleProcessorConfigurationController<FooBarConfiguration> implements FormBasedConfigurationController<FooBarConfiguration>
{
    public FooBarConfigurationController ()
    {
        super ( FooBarProcessorFactory.ID, "foobar/configuration" );
    }

    @Override
    protected FooBarConfiguration newModel ()
    {
        return new FooBarConfiguration ();
    }

    @Override
    protected FooBarConfiguration parseModel ( final String configuration )
    {
        return FooBarConfiguration.fromJson ( configuration );
    }

    @Override
    protected String writeModel ( final FooBarConfiguration configuration )
    {
        return configuration.toJson ();
    }
}
