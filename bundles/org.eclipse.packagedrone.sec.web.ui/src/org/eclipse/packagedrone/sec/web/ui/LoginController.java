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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.sec.DatabaseDetails;
import org.eclipse.packagedrone.sec.UserInformation;
import org.eclipse.packagedrone.sec.UserInformationPrincipal;
import org.eclipse.packagedrone.sec.service.LoginException;
import org.eclipse.packagedrone.sec.service.SecurityService;
import org.eclipse.packagedrone.sec.web.filter.SecurityFilter;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.form.FormData;
import org.eclipse.scada.utils.ExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/login" )
public class LoginController
{
    private final static Logger logger = LoggerFactory.getLogger ( LoginController.class );

    private SecurityService service;

    public void setService ( final SecurityService service )
    {
        this.service = service;
    }

    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView login ( final HttpServletRequest request )
    {
        final Map<String, Object> model = new HashMap<> ();

        final LoginData data = new LoginData ();

        final Cookie[] cookies = request.getCookies ();
        if ( cookies != null )
        {
            for ( final Cookie cookie : cookies )
            {
                if ( cookie.getName ().equals ( SecurityFilter.COOKIE_EMAIL ) )
                {
                    data.setEmail ( cookie.getValue () );
                }
            }
        }

        model.put ( "command", data );
        model.put ( "showAdminMode", needShowAdminMode () );

        return new ModelAndView ( "login/form", model );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView loginPost ( @FormData ( "command" ) final LoginData data, final HttpServletRequest request, final HttpServletResponse response)
    {
        try
        {
            request.setAttribute ( SecurityFilter.ATTR_REMEMBER_ME, data.isRememberMeSafe () );

            request.login ( data.getEmail (), data.getPassword () );

            if ( data.isRememberMeSafe () )
            {
                final Principal p = request.getUserPrincipal ();
                if ( p instanceof UserInformationPrincipal )
                {
                    final UserInformationPrincipal uip = (UserInformationPrincipal)p;

                    final UserInformation ui = uip.getUserInformation ();

                    if ( ui != null )
                    {
                        final DatabaseDetails dd = ui.getDetails ( DatabaseDetails.class );

                        final String token = ui.getRememberMeToken ();
                        if ( token != null )
                        {
                            Cookie cookie = new Cookie ( SecurityFilter.COOKIE_REMEMBER_ME, token );
                            cookie.setMaxAge ( (int)TimeUnit.DAYS.toSeconds ( 90 ) );
                            response.addCookie ( cookie );

                            if ( dd != null && dd.getEmail () != null )
                            {
                                cookie = new Cookie ( SecurityFilter.COOKIE_EMAIL, dd.getEmail () );
                                cookie.setMaxAge ( (int)TimeUnit.DAYS.toSeconds ( 360 ) );
                                response.addCookie ( cookie );
                            }
                        }
                    }
                }
            }
        }
        catch ( final ServletException e )
        {
            final Map<String, Object> model = new HashMap<> ();

            final Throwable root = ExceptionHelper.getRootCause ( e );
            if ( root instanceof LoginException )
            {
                model.put ( "errorTitle", root.getMessage () );
                model.put ( "details", ( (LoginException)root ).getDetails () );

                final long failures = Sessions.incrementLoginFailCounter ( request.getSession () );
                model.put ( "failureCount", failures );
            }
            else
            {
                logger.warn ( "Login error", e );
                model.put ( "errorTitle", "Internal error!" );
                model.put ( "details", String.format ( "Failed to log in: %s", root.getClass ().getSimpleName () ) );
            }

            model.put ( "showAdminMode", needShowAdminMode () );

            return new ModelAndView ( "login/form", model );
        }

        Sessions.resetLoginFailCounter ( request.getSession () );

        return new ModelAndView ( "redirect:/" );
    }

    private boolean needShowAdminMode ()
    {
        return !this.service.hasUserBase ();
    }

}
