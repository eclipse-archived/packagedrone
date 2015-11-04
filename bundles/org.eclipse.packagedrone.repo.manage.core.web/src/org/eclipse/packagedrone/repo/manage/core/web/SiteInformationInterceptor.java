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
package org.eclipse.packagedrone.repo.manage.core.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.manage.core.CoreService;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.interceptor.ModelAndViewInterceptorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteInformationInterceptor extends ModelAndViewInterceptorAdapter
{

    private final static Logger logger = LoggerFactory.getLogger ( SiteInformationInterceptor.class );

    private CoreService service;

    public void setService ( final CoreService service )
    {
        this.service = service;
    }

    @Override
    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView ) throws Exception
    {
        modelAndView.put ( "siteInformation", getSiteInformation () );
    }

    private SiteInformation getSiteInformation ()
    {
        try
        {
            final Map<MetaKey, String> all = this.service.list ();
            final SiteInformation data = new SiteInformation ();
            MetaKeys.bind ( data, all );
            return data;
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to get site information", e );
            return null;
        }
    }
}
