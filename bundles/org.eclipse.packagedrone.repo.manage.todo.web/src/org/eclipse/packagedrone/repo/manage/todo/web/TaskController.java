/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.manage.todo.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;

import org.eclipse.packagedrone.repo.manage.todo.ToDoService;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/tasks" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class TaskController
{
    private ToDoService service;

    public void setService ( final ToDoService service )
    {
        this.service = service;
    }

    @RequestMapping
    public ModelAndView view ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "tasks", this.service.getOpenTasks () );

        return new ModelAndView ( "view", model );
    }
}
