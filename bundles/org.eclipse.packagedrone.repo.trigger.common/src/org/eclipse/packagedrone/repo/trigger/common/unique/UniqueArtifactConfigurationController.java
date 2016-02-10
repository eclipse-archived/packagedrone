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
package org.eclipse.packagedrone.repo.trigger.common.unique;

import javax.servlet.annotation.HttpConstraint;

import org.eclipse.packagedrone.repo.trigger.common.SimpleConfigurationController;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/trigger/processor.factory/unique.artifact/configure" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class UniqueArtifactConfigurationController extends SimpleConfigurationController<UniqueArtifactConfiguration>
{
    public UniqueArtifactConfigurationController ()
    {
        super ( UniqueArtifactProcessorFactory.ID, "unique.artifact/configuration" );
    }

    @Override
    protected UniqueArtifactConfiguration newModel ()
    {
        return new UniqueArtifactConfiguration ();
    }

    @Override
    protected UniqueArtifactConfiguration parseModel ( final String configuration )
    {
        return UniqueArtifactConfiguration.fromJson ( configuration );
    }

    @Override
    protected String writeModel ( final UniqueArtifactConfiguration configuration )
    {
        return configuration.toJson ();
    }
}
