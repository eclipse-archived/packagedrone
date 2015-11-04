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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.manage.todo.ToDoService;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.interceptor.ModelAndViewInterceptorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToDoInterceptor extends ModelAndViewInterceptorAdapter
{
    private final static Logger logger = LoggerFactory.getLogger ( ToDoInterceptor.class );

    private volatile ToDoService service;

    public void setService ( final ToDoService service )
    {
        this.service = service;
    }

    public void unsetService ( final ToDoService service )
    {
        this.service = null;
    }

    @Override
    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView ) throws Exception
    {
        final ToDoService service = this.service;
        if ( service != null )
        {
            try
            {
                modelAndView.put ( "openTasks", service.getOpenTasks () );
            }
            catch ( final Exception e )
            {
                logger.warn ( "Failed to get open tasks", e );
            }
        }
    }

}
