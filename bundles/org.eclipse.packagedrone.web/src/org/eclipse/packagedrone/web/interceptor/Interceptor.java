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

public interface Interceptor
{
    public boolean preHandle ( HttpServletRequest request, HttpServletResponse response ) throws Exception;

    public void postHandle ( HttpServletRequest request, HttpServletResponse response, RequestHandler requestHandler ) throws Exception;

    public void afterCompletion ( HttpServletRequest request, HttpServletResponse response, Exception ex ) throws Exception;
}
