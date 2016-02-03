/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.eclipse.packagedrone.web.controller.Controllers;
import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation;
import org.eclipse.scada.utils.str.StringReplacer;
import org.eclipse.scada.utils.str.StringReplacer.ReplaceSource;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

public class LinkTarget
{
    public class EscaperSource implements ReplaceSource
    {
        private final ReplaceSource source;

        private final Escaper escaper;

        public EscaperSource ( final ReplaceSource source, final Escaper escaper )
        {
            this.source = source;
            this.escaper = escaper;
        }

        @Override
        public String replace ( final String context, final String key )
        {
            final String result = this.source.replace ( context, key );

            if ( result != null )
            {
                return this.escaper.escape ( result );
            }
            else
            {
                return result;
            }
        }
    }

    private static final Pattern PATTERN = Pattern.compile ( "\\{(.*?)\\}" );

    private final String url;

    public LinkTarget ( final String url )
    {
        this.url = url;
    }

    public String render ( final ServletRequest request )
    {
        return expandSource ( new ReplaceSource () {

            @Override
            public String replace ( final String context, final String key )
            {
                final Object v = request.getAttribute ( key );
                if ( v == null )
                {
                    return context;
                }
                else
                {
                    return v.toString ();
                }
            }
        } ).getUrl ();
    }

    public String render ( final PageContext pageContext )
    {
        return render ( pageContext.getRequest () );
    }

    public String renderFull ( final PageContext pageContext )
    {
        final StringBuilder sb = new StringBuilder ( pageContext.getServletContext ().getContextPath () );

        if ( sb.length () > 0 && !sb.substring ( sb.length () - 1 ).equals ( "/" ) )
        {
            sb.append ( '/' );
        }

        sb.append ( render ( pageContext.getRequest () ) );

        return sb.toString ();
    }

    public String render ( final Map<String, ?> model )
    {
        return expandSource ( StringReplacer.newExtendedSource ( model ) ).getUrl ();
    }

    public LinkTarget expand ( final Map<String, ?> model )
    {
        return expandSource ( StringReplacer.newExtendedSource ( model ) );
    }

    public LinkTarget expandSource ( final ReplaceSource source )
    {
        if ( this.url == null || source == null )
        {
            return this;
        }

        final ReplaceSource encodeSource = new EscaperSource ( source, UrlEscapers.urlPathSegmentEscaper () );

        return new LinkTarget ( StringReplacer.replace ( this.url, encodeSource, PATTERN, false ) );
    }

    public String getUrl ()
    {
        return this.url;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[LinkTarget: %s]", this.url );
    }

    private static Set<String> getRawPaths ( final Class<?> controllerClazz, final Method method )
    {
        final RequestMappingInformation rmi = Controllers.fromMethod ( controllerClazz, method );
        if ( rmi == null )
        {
            return null;
        }

        return rmi.getRawPaths ();
    }

    public static LinkTarget createFromController ( final ControllerMethod m )
    {
        final Set<String> paths = getRawPaths ( m.getControllerClazz (), m.getMethod () );

        if ( paths != null && !paths.isEmpty () )
        {
            return new LinkTarget ( paths.iterator ().next () );
        }

        throw new IllegalArgumentException ( String.format ( "Controller class '%s' has no request method '%s'", m.getControllerClazz ().getName (), m.getMethod ().getName () ) );
    }

    public static LinkTarget createFromController ( final Class<?> controllerClazz, final String methodName )
    {
        final ControllerMethod m = getControllerMethod ( controllerClazz, methodName );

        if ( m != null )
        {
            return createFromController ( m );
        }

        throw new IllegalArgumentException ( String.format ( "Controller class '%s' has no request method '%s'", controllerClazz.getName (), methodName ) );
    }

    public static class ControllerMethod
    {
        private final Class<?> controllerClazz;

        private final Method method;

        public ControllerMethod ( final Class<?> controllerClazz, final Method method )
        {
            this.controllerClazz = controllerClazz;
            this.method = method;
        }

        public Class<?> getControllerClazz ()
        {
            return this.controllerClazz;
        }

        public Method getMethod ()
        {
            return this.method;
        }
    }

    public static ControllerMethod getControllerMethod ( final Class<?> controllerClazz, final String methodName )
    {
        for ( final Method m : controllerClazz.getMethods () )
        {
            if ( !m.getName ().equals ( methodName ) )
            {
                continue;
            }

            return new ControllerMethod ( controllerClazz, m );
        }
        return null;
    }

    /*
    public static LinkTarget createFromController ( final Method method )
    {
        if ( method == null )
        {
            throw new IllegalStateException ( "No method provided" );
        }

        final Set<String> paths = getRawPaths ( method );
        if ( paths.isEmpty () )
        {
            throw new IllegalStateException ( String.format ( "Method '%s' has no @RequestMapping information assigned", method ) );
        }

        return new LinkTarget ( paths.iterator ().next () );
    }
    */
}
