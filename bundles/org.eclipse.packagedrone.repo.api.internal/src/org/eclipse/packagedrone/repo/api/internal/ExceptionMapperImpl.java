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
package org.eclipse.packagedrone.repo.api.internal;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.packagedrone.repo.api.ErrorInformation;
import org.eclipse.scada.utils.ExceptionHelper;

@Provider
public class ExceptionMapperImpl implements ExceptionMapper<Exception>
{
    @Override
    public Response toResponse ( final Exception error )
    {
        return Response.serverError ().entity ( makeError ( error ) ).build ();
    }

    private ErrorInformation makeError ( final Exception error )
    {
        final ErrorInformation errorInformation = new ErrorInformation ();

        errorInformation.setCode ( 1 );
        errorInformation.setMessage ( ExceptionHelper.getMessage ( error ) );

        return errorInformation;
    }
}
