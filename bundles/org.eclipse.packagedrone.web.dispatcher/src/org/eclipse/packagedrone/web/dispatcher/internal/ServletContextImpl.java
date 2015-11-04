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
package org.eclipse.packagedrone.web.dispatcher.internal;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.dispatcher.DispatcherHttpContext;
import org.ops4j.pax.web.service.WebContainerContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextImpl implements WebContainerContext, DispatcherHttpContext
{
    private final static Logger logger = LoggerFactory.getLogger ( ServletContextImpl.class );

    private final List<ResourceProvider> sources = new LinkedList<> ();

    private final Bundle bundle;

    public ServletContextImpl ( final BundleContext context )
    {
        this.bundle = context.getBundle ();

        this.sources.add ( new TagDirTracker ( context ) );
        this.sources.add ( new TagLibTracker ( context, "/WEB-INF/" ) );
    }

    @Override
    public String toString ()
    {
        return String.format ( "[HttpContext for: %s / %s]", this.bundle.getBundleId (), this.bundle.getSymbolicName () );
    }

    @Override
    public String getMimeType ( final String name )
    {
        return null;
    }

    @Override
    public boolean handleSecurity ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        return true;
    }

    @Override
    public URL getResource ( final String name )
    {
        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource: {}", name );
        }

        final URL result = internalGetResource ( name );

        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource: {} -> {}", name, result );
        }

        return result;
    }

    @Override
    public Set<String> getResourcePaths ( final String path )
    {
        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource paths: {}", path );
        }

        final Set<String> result = new HashSet<> ();

        internalGetResourcePaths ( path, result );

        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource paths: {} -> {}", path, result );
        }

        return result.isEmpty () ? null : result;
    }

    protected URL internalGetResource ( final String name )
    {
        for ( final ResourceProvider provider : this.sources )
        {
            final URL result = provider.getResource ( name );
            if ( result != null )
            {
                return result;
            }
        }

        return null;
    }

    protected void internalGetResourcePaths ( final String path, final Set<String> result )
    {
        for ( final ResourceProvider provider : this.sources )
        {
            final Set<String> providerResult = provider.getPaths ( path );
            if ( providerResult != null )
            {
                result.addAll ( providerResult );
            }
        }
    }

    @Override
    public void dispose ()
    {
        for ( final ResourceProvider provider : this.sources )
        {
            provider.dispose ();
        }
    }

}
