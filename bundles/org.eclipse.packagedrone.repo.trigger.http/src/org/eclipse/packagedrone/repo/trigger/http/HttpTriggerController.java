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

import java.util.Map;

import javax.servlet.annotation.HttpConstraint;

import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.repo.trigger.common.SimpleTriggerConfigurationController;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/trigger/factory/" + HttpTriggerFactory.ID + "/configure" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class HttpTriggerController extends SimpleTriggerConfigurationController<HttpTriggerConfiguration>
{
    private SitePrefixService sitePrefixService;

    public void setSitePrefixService ( final SitePrefixService sitePrefixService )
    {
        this.sitePrefixService = sitePrefixService;
    }

    public HttpTriggerController ()
    {
        super ( HttpTriggerFactory.ID, "http.endpoint/configuration" );
    }

    @Override
    protected HttpTriggerConfiguration newModel ()
    {
        return new HttpTriggerConfiguration ();
    }

    @Override
    protected HttpTriggerConfiguration parseModel ( final String configuration )
    {
        return HttpTriggerConfiguration.fromJson ( configuration );
    }

    @Override
    protected String writeModel ( final HttpTriggerConfiguration configuration )
    {
        return configuration.toJson ();
    }

    @Override
    protected void fillModel ( final Map<String, Object> model )
    {
        model.put ( "sitePrefix", this.sitePrefixService.getSitePrefix () );
    }
}
