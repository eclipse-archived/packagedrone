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
package org.eclipse.packagedrone.web.util;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.google.common.collect.Iterators;

public class ParameterOverridingRequestWrapper extends HttpServletRequestWrapper
{
    private final Map<String, String[]> parameters;

    public ParameterOverridingRequestWrapper ( final HttpServletRequest request, final Map<String, String[]> parameters )
    {
        super ( request );
        this.parameters = parameters;
    }

    @Override
    public String[] getParameterValues ( final String name )
    {
        return getParameterMap ().get ( name );
    }

    @Override
    public Enumeration<String> getParameterNames ()
    {
        return Iterators.asEnumeration ( getParameterMap ().keySet ().iterator () );
    }

    @Override
    public String getParameter ( final String name )
    {
        final String[] values = getParameterValues ( name );
        if ( values == null || values.length < 1 )
        {
            return null;
        }
        return values[0];
    }

    @Override
    public Map<String, String[]> getParameterMap ()
    {
        return this.parameters;
    }
}
