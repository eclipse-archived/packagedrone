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
package org.eclipse.packagedrone.sec.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.packagedrone.sec.UserInformation;
import org.eclipse.packagedrone.sec.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityFilter implements Filter
{
    private final static Logger logger = LoggerFactory.getLogger ( SecurityFilter.class );

    protected static final String ATTR_USER_INFORMATION = SecurityFilter.class.getName () + ".userInformation";

    protected static final String ATTR_USER_RELOAD = SecurityFilter.class.getName () + ".reload";

    public static final String ATTR_REMEMBER_ME = SecurityFilter.class.getName () + ".rememberMe";

    public static final String COOKIE_REMEMBER_ME = "rememberMe";

    public static final String COOKIE_EMAIL = "email";

    private SecurityService service;

    public void setService ( final SecurityService service )
    {
        this.service = service;
    }

    public void unsetService ( final SecurityService service )
    {
        this.service = null;
    }

    @Override
    public void init ( final FilterConfig filterConfig ) throws ServletException
    {
    }

    @Override
    public void destroy ()
    {
    }

    @Override
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain filterChain ) throws IOException, ServletException
    {
        logger.trace ( "doFilter[{}] - {}", this.service != null, request );

        if ( this.service != null && request instanceof HttpServletRequest && response instanceof HttpServletResponse )
        {
            logger.trace ( "Injecting request wrapper" );
            processFilter ( (HttpServletRequest)request, (HttpServletResponse)response, filterChain );
        }
        else
        {
            // just pretend we are not here, so we also won't inject any user principals
            filterChain.doFilter ( request, response );
        }
    }

    protected void processFilter ( final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain ) throws IOException, ServletException
    {
        logger.trace ( "Processing request: {}", request );

        final HttpSession session = request.getSession ();

        if ( session != null )
        {
            // always reload details

            markReloadDetails ( session );

            // try to log on

            tryRememberMe ( request, response, session );

        }

        filterChain.doFilter ( new SecurityHttpRequestWrapper ( this.service, request ), response );
    }

    protected void tryRememberMe ( final HttpServletRequest request, final HttpServletResponse response, final HttpSession session )
    {
        final Object userObj = session.getAttribute ( ATTR_USER_INFORMATION );
        if ( userObj == null )
        {
            final String token = findCookie ( COOKIE_REMEMBER_ME, request.getCookies () );
            final String email = findCookie ( COOKIE_EMAIL, request.getCookies () );

            if ( token != null && email != null )
            {
                try
                {
                    final UserInformation user = this.service.login ( email, token );
                    logger.info ( "Tried to log in using rememberMe token: {} -> {} for {}", email, user, request );
                    applyUserInformation ( request, user );
                }
                catch ( final Exception e )
                {
                    // silently ignore the failure, but delete to cookie
                    logger.info ( "Failed to login in by 'rember me', deleting cookie", e );

                    final Cookie cookie = new Cookie ( SecurityFilter.COOKIE_REMEMBER_ME, token );
                    cookie.setMaxAge ( 0 );
                    response.addCookie ( cookie );
                }
            }
        }
    }

    private String findCookie ( final String name, final Cookie[] cookies )
    {
        if ( cookies != null )
        {
            for ( final Cookie cookie : cookies )
            {
                if ( cookie.getName ().equals ( name ) )
                {
                    return cookie.getValue ();
                }
            }
        }
        return null;
    }

    public static void markReloadDetails ( final HttpSession session )
    {
        if ( session == null )
        {
            return;
        }

        session.setAttribute ( ATTR_USER_RELOAD, true );
    }

    public static boolean isLoggedIn ( final HttpServletRequest request )
    {
        return request.getUserPrincipal () != null;
    }

    public static void applyUserInformation ( final HttpServletRequest request, final UserInformation user )
    {
        final HttpSession session = request.getSession ();

        // set the new user
        session.setAttribute ( SecurityFilter.ATTR_USER_INFORMATION, user );

        // remove the reload marker, we just did that
        session.removeAttribute ( SecurityFilter.ATTR_USER_RELOAD );
    }

}
