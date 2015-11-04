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
package org.eclipse.packagedrone.sec.web.ui;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.sec.web.filter.SecurityFilter;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/logout" )
public class LogoutController
{
    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView logout ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException
    {
        request.logout ();

        // delete remember me cookie

        final Cookie cookie = new Cookie ( SecurityFilter.COOKIE_REMEMBER_ME, null );
        cookie.setMaxAge ( 0 );
        response.addCookie ( cookie );

        return new ModelAndView ( "redirect:/" );
    }
}
