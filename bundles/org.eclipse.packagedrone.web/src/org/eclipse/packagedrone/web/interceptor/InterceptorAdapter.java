/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.RequestHandler;

public class InterceptorAdapter implements Interceptor
{

    @Override
    public boolean preHandle ( final HttpServletRequest request, final HttpServletResponse response ) throws Exception
    {
        return true;
    }

    @Override
    public void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler ) throws Exception
    {
    }

    @Override
    public void afterCompletion ( final HttpServletRequest request, final HttpServletResponse response, final Exception ex ) throws Exception
    {
    }

}
