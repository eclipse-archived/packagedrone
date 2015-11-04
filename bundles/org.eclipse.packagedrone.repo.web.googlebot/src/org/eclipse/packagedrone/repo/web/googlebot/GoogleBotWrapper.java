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
package org.eclipse.packagedrone.repo.web.googlebot;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class GoogleBotWrapper extends HttpServletResponseWrapper
{
    public GoogleBotWrapper ( final HttpServletResponse response )
    {
        super ( response );
    }

    @Override
    public String encodeRedirectUrl ( final String url )
    {
        return url;
    }

    @Override
    public String encodeRedirectURL ( final String url )
    {
        return url;
    }

    @Override
    public String encodeUrl ( final String url )
    {
        return url;
    }

    @Override
    public String encodeURL ( final String url )
    {
        return url;
    }
}
