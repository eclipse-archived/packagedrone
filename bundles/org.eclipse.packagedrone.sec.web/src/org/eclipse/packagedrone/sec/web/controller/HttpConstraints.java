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

import java.lang.reflect.Method;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

public class HttpConstraints
{
    private HttpConstraints ()
    {
    }

    public static boolean isCallAllowed ( final Method m, final HttpServletRequest request )
    {
        HttpConstraint constraint = m.getAnnotation ( HttpConstraint.class );

        if ( constraint == null )
        {
            constraint = m.getDeclaringClass ().getAnnotation ( HttpConstraint.class );
        }

        if ( constraint == null )
        {
            return true;
        }

        return HttpContraintControllerInterceptor.isAllowed ( constraint, request );
    }
}
