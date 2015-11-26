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
package org.eclipse.packagedrone.repo.web.sitemap.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;

public class SitemapServlet extends HttpServlet
{
    private static final String SITEMAP_XML = "/sitemap.xml";

    private static final long serialVersionUID = 1L;

    private XmlToolsFactory xml;

    private SitePrefixService prefixService;

    private SitemapProcessor processor;

    public void setXml ( final XmlToolsFactory xml )
    {
        this.xml = xml;
    }

    public void setPrefixService ( final SitePrefixService prefixService )
    {
        this.prefixService = prefixService;
    }

    @Override
    public void init () throws ServletException
    {
        super.init ();
        this.processor = new SitemapProcessor ( this.prefixService::getSitePrefix, SITEMAP_XML, this.xml.newXMLOutputFactory () );
    }

    @Override
    public void destroy ()
    {
        super.destroy ();
        this.processor.dispose ();
    }

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        final String path = req.getRequestURI ();

        if ( path == null || !path.startsWith ( SITEMAP_XML ) )
        {
            handleNotFound ( req, resp );
        }
        else
        {
            String subPath = path.substring ( SITEMAP_XML.length () );

            if ( subPath.startsWith ( "/" ) )
            {
                // cut off leading slash
                subPath = subPath.substring ( 1 );
            }

            if ( !this.processor.process ( resp, subPath ) )
            {
                handleNotFound ( req, resp );
            }
        }
    }

    private void handleNotFound ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        response.setContentType ( "text/plain" );
        response.getWriter ().format ( "Resource '%s' could not be found", request.getRequestURI () );
    }
}
