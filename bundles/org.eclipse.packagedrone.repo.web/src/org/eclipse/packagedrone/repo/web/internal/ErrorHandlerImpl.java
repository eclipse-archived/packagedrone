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
package org.eclipse.packagedrone.repo.web.internal;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.ErrorHandler;
import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandlerImpl implements ErrorHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( ErrorHandlerImpl.class );

    private final String errorResource;

    public ErrorHandlerImpl ()
    {
        final Bundle bundle = FrameworkUtil.getBundle ( ErrorHandlerImpl.class );
        this.errorResource = String.format ( "/bundle/%s/WEB-INF/views/error.jsp", bundle.getBundleId () );
    }

    @Override
    public void handleError ( final HttpServletRequest request, final HttpServletResponse response, final Throwable e ) throws ServletException, IOException
    {
        logger.warn ( "Handling UI error", e );

        response.setStatus ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );

        request.setAttribute ( "result", "Operation failed" );
        request.setAttribute ( "message", ExceptionHelper.getMessage ( e ) );
        request.setAttribute ( "exception", e );
        request.setAttribute ( "stacktrace", ExceptionHelper.formatted ( e ) );

        final RequestDispatcher rd = request.getRequestDispatcher ( this.errorResource );
        if ( response.isCommitted () )
        {
            rd.include ( request, response );
        }
        else
        {
            rd.forward ( request, response );
        }
    }
}
