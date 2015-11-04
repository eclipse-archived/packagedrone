/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.RequestHandler;

public interface ControllerInterceptorProcessor
{

    public default RequestHandler before ( final Object controller, final Method m, final HttpServletRequest request, final HttpServletResponse response, final BiFunction<HttpServletRequest, HttpServletResponse, RequestHandler> next ) throws Exception
    {
        return next.apply ( request, response );
    }

    public default RequestHandler after ( final Object controller, final Method m, final HttpServletRequest request, final HttpServletResponse response, final BiFunction<HttpServletRequest, HttpServletResponse, RequestHandler> next ) throws Exception
    {
        return next.apply ( request, response );
    }

}
