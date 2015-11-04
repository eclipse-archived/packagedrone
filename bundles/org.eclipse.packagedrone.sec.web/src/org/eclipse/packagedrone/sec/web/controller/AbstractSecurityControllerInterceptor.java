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
package org.eclipse.packagedrone.sec.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.RedirectRequestHandler;
import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.common.CommonController;
import org.eclipse.packagedrone.web.controller.ControllerInterceptorProcessor;

public abstract class AbstractSecurityControllerInterceptor implements ControllerInterceptorProcessor
{
    protected RequestHandler handleAccessDenied ( final HttpServletResponse response ) throws IOException
    {
        return CommonController.wrap ( CommonController::createAccessDenied );
    }

    protected RequestHandler handleLoginRequired ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        /*
         * we could store the request here and unpack it later.
         *
         * however this turns out to be quite an effort. storing the request
         * would not only mean storing the request, path and query, but also
         * the cookies, headers and maybe more ...
         */
        return new RedirectRequestHandler ( "/login" );
    }

}
