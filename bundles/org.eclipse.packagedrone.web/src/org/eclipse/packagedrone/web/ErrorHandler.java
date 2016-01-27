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
package org.eclipse.packagedrone.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ErrorHandler
{
    public static final ErrorHandler DEFAULT = new ErrorHandler () {

        @Override
        public void handleError ( final HttpServletRequest request, final HttpServletResponse response, final Throwable e ) throws ServletException
        {
            if ( e instanceof ServletException )
            {
                throw (ServletException)e;
            }
            throw new ServletException ( e );
        }
    };

    public void handleError ( HttpServletRequest request, HttpServletResponse response, Throwable e ) throws ServletException, IOException;
}
