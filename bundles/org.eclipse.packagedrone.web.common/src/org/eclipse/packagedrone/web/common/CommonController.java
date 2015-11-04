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
package org.eclipse.packagedrone.web.common;

import java.util.function.Supplier;

import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.ModelAndViewRequestHandler;
import org.eclipse.scada.utils.ExceptionHelper;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class CommonController
{
    public static final String SUCCESS_VIEW = "common/success";

    public static final String SUCCESS_VIEW_TITLE = "title";

    public static final String SUCCESS_VIEW_SUBTITLE = "subtitle";

    public static final String SUCCESS_VIEW_MESSAGE = "message";

    public static final String ACCESS_DENIED_VIEW = "common/accessDenied";

    public static final String ERROR_VIEW = "common/error";

    public static final String ERROR_VIEW_TITLE = "title";

    public static final String ERROR_VIEW_SUBTITLE = "subtitle";

    public static final String ERROR_VIEW_RESULT = "result";

    public static final String ERROR_VIEW_MESSAGE = "message";

    public static final String ERROR_VIEW_STACKTRACE = "stacktrace";

    public static final String ERROR_VIEW_EXCEPTION = "exception";

    public static final String NOT_FOUND_VIEW = "common/notFound";

    public static final String NOT_FOUND_VIEW_TYPE = "type";

    public static final String NOT_FOUND_VIEW_ID = "id";

    public static ModelAndView createNotFound ( final String type, final String id )
    {
        final ModelAndView result = new ModelAndView ( NOT_FOUND_VIEW );

        result.put ( NOT_FOUND_VIEW_ID, id );
        result.put ( NOT_FOUND_VIEW_TYPE, type );

        result.setAlternateViewResolver ( CommonController.class );

        return result;
    }

    public static ModelAndView createError ( final String title, final String result, final Throwable e, final Boolean showStackTrace )
    {
        return createError ( title, null, result, e, showStackTrace );
    }

    public static ModelAndView createError ( final String title, final String subtitle, final String result, final Throwable e, final Boolean showStackTrace )
    {
        final ModelAndView mav = new ModelAndView ( ERROR_VIEW );

        if ( showStackTrace != null )
        {
            mav.put ( "showStackTrace", showStackTrace );
        }
        else
        {
            mav.put ( "showStackTrace", Boolean.getBoolean ( "drone.showStackTrace" ) );
        }

        mav.put ( ERROR_VIEW_TITLE, title );
        mav.put ( ERROR_VIEW_SUBTITLE, subtitle );
        mav.put ( ERROR_VIEW_RESULT, result );
        mav.put ( ERROR_VIEW_EXCEPTION, e );
        if ( e != null )
        {
            mav.put ( ERROR_VIEW_MESSAGE, ExceptionHelper.getMessage ( e ) );
            mav.put ( ERROR_VIEW_STACKTRACE, ExceptionHelper.formatted ( e ) );
        }

        mav.setAlternateViewResolver ( CommonController.class );

        return mav;
    }

    public static ModelAndView createError ( final String title, final String result, final Throwable e )
    {
        return createError ( title, result, e, null );
    }

    public static ModelAndView createAccessDenied ()
    {
        final ModelAndView mav = new ModelAndView ( ACCESS_DENIED_VIEW );

        mav.setAlternateViewResolver ( CommonController.class );

        return mav;
    }

    public static ModelAndView createSuccess ( final String title, final String subtitle, final String message )
    {
        final ModelAndView mav = new ModelAndView ( SUCCESS_VIEW );

        mav.put ( SUCCESS_VIEW_TITLE, title );
        mav.put ( SUCCESS_VIEW_SUBTITLE, subtitle );
        mav.put ( SUCCESS_VIEW_MESSAGE, message );

        mav.setAlternateViewResolver ( CommonController.class );

        return mav;
    }

    public static ModelAndViewRequestHandler wrap ( final Supplier<ModelAndView> supplier )
    {
        return new ModelAndViewRequestHandler ( supplier.get (), CommonController.class, null );
    }

}
