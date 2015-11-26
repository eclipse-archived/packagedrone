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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.manage.core.CoreService;
import org.eclipse.packagedrone.repo.manage.system.SystemService;
import org.eclipse.packagedrone.sec.web.controller.HttpConstraints;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.form.FormData;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@RequestMapping ( "/config/core" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class CoreController implements InterfaceExtender
{
    private CoreService coreService;

    private SystemService systemService;

    private static final Method METHOD_LIST = LinkTarget.getControllerMethod ( CoreController.class, "list" );

    private static final Method METHOD_SITE = LinkTarget.getControllerMethod ( CoreController.class, "site" );

    public void setCoreService ( final CoreService service )
    {
        this.coreService = service;
    }

    public void setSystemService ( final SystemService systemService )
    {
        this.systemService = systemService;
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( HttpConstraints.isCallAllowed ( METHOD_LIST, request ) )
        {
            result.add ( new MenuEntry ( "Administration", 1000, "View properties", 1000, LinkTarget.createFromController ( METHOD_LIST ), null, null ) );
        }
        if ( HttpConstraints.isCallAllowed ( METHOD_SITE, request ) )
        {
            result.add ( new MenuEntry ( "Administration", 1000, "Site", 500, LinkTarget.createFromController ( METHOD_SITE ), null, null ) );
        }

        return result;
    }

    @RequestMapping ( value = "/list" )
    public ModelAndView list ()
    {
        final Map<String, Object> model = new HashMap<> ( 1 );

        model.put ( "properties", new TreeMap<> ( this.coreService.list () ) );

        return new ModelAndView ( "list", model );
    }

    @RequestMapping ( value = "/site" )
    public ModelAndView site ()
    {
        final Map<String, Object> model = new HashMap<> ();

        final Map<MetaKey, String> props = this.coreService.list ();

        final SiteInformation site = new SiteInformation ();
        try
        {
            MetaKeys.bind ( site, props );
        }
        catch ( final Exception e )
        {
            // use plain new object
        }

        fillModel ( model, site );

        return new ModelAndView ( "site", model );
    }

    protected void fillModel ( final Map<String, Object> model, final SiteInformation site )
    {
        model.put ( "command", site );
        model.put ( "defaultSitePrefix", this.systemService.getDefaultSitePrefix () );
    }

    @RequestMapping ( value = "/site", method = RequestMethod.POST )
    public ModelAndView sitePost ( @Valid @FormData ( "command" ) final SiteInformation site, final BindingResult result) throws Exception
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !result.hasErrors () )
        {
            // store
            final Map<MetaKey, String> props = MetaKeys.unbind ( site );

            this.coreService.setCoreProperties ( props );
        }

        fillModel ( model, site );

        return new ModelAndView ( "site", model );
    }
}
