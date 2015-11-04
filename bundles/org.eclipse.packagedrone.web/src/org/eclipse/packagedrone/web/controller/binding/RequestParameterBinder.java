/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller.binding;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.eclipse.packagedrone.utils.converter.ConverterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestParameterBinder implements Binder
{
    private final static Logger logger = LoggerFactory.getLogger ( RequestParameterBinder.class );

    private final HttpServletRequest request;

    public RequestParameterBinder ( final HttpServletRequest request )
    {
        this.request = request;
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        final RequestParameter rp = target.getAnnotation ( RequestParameter.class );
        if ( rp == null )
        {
            return null;
        }

        final Class<?> type = target.getType ();

        if ( type.isAssignableFrom ( Part.class ) )
        {
            try
            {
                final Part value = this.request.getPart ( rp.value () );
                return Binding.simpleBinding ( value );
            }
            catch ( final Exception e )
            {
                logger.info ( "Failed to get part '{}'", rp.value () );
                return null;
            }
        }
        else
        {
            final String valueString = this.request.getParameter ( rp.value () );

            try
            {

                if ( valueString == null )
                {
                    if ( rp.required () )
                    {
                        throw new IllegalStateException ( String.format ( "Request parameter '%s' is required but missing.", rp.value () ) );
                    }

                    final Object value = converter.convertTo ( null, type );
                    return Binding.simpleBinding ( value );
                }
                else
                {

                    final Object value = converter.convertTo ( valueString, type );
                    return Binding.simpleBinding ( value );
                }
            }
            catch ( final Exception e )
            {
                logger.debug ( "Failed to bind", e );
                return Binding.errorBinding ( e );
            }
        }
    }
}
