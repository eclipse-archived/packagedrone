/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.sec.web.controller;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.web.LinkTarget.ControllerMethod;

public class HttpConstraints
{
    private HttpConstraints ()
    {
    }

    public static boolean isCallAllowed ( final ControllerMethod m, final HttpServletRequest request )
    {
        HttpConstraint constraint = m.getMethod ().getAnnotation ( HttpConstraint.class );

        if ( constraint == null )
        {
            constraint = m.getControllerClazz ().getAnnotation ( HttpConstraint.class );
        }

        if ( constraint == null )
        {
            return true;
        }

        return HttpContraintControllerInterceptor.isAllowed ( constraint, request );
    }
}
