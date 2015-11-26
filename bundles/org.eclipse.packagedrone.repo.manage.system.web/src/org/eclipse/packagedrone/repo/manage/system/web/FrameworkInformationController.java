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
package org.eclipse.packagedrone.repo.manage.system.web;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

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
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;

@Controller
@ViewResolver ( "/WEB-INF/views/info/framework/%s.jsp" )
@RequestMapping ( "/system/info/framework" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class FrameworkInformationController implements InterfaceExtender
{
    private final BundleContext context = FrameworkUtil.getBundle ( FrameworkInformationController.class ).getBundleContext ();

    @RequestMapping
    public ModelAndView view ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "vendor", System.getProperty ( Constants.FRAMEWORK_VENDOR ) );
        model.put ( "version", System.getProperty ( Constants.FRAMEWORK_VERSION ) );

        final Bundle sysBundle = this.context.getBundle ( Constants.SYSTEM_BUNDLE_ID );
        if ( sysBundle != null )
        {
            model.put ( "sysSymbolicName", sysBundle.getSymbolicName () );
            model.put ( "sysName", sysBundle.getHeaders ( null ).get ( Constants.BUNDLE_NAME ) );
        }

        return new ModelAndView ( "index", model );
    }

    @RequestMapping ( "/bundles" )
    public ModelAndView bundles ()
    {
        final Map<String, Object> model = new HashMap<> ();

        final Bundle[] bundles = this.context.getBundles ();
        Arrays.sort ( bundles, Comparator.comparing ( Bundle::getSymbolicName ) );
        model.put ( "bundles", stream ( bundles ).map ( BundleInformation::new ).collect ( toList () ) );

        return new ModelAndView ( "bundles", model );
    }

    @RequestMapping ( value = "/bundles/{id}/start", method = RequestMethod.POST )
    public ModelAndView startBundle ( @PathVariable ( "id" ) final long bundleId) throws BundleException
    {
        final Bundle bundle = this.context.getBundle ( bundleId );
        if ( bundle != null )
        {
            bundle.start ();
        }
        return new ModelAndView ( "redirect:/system/info/framework/bundles" );
    }

    @RequestMapping ( value = "/bundles/{id}/stop", method = RequestMethod.POST )
    public ModelAndView stopBundle ( @PathVariable ( "id" ) final long bundleId) throws BundleException
    {
        final Bundle bundle = this.context.getBundle ( bundleId );
        if ( bundle != null )
        {
            bundle.stop ();
        }
        return new ModelAndView ( "redirect:/system/info/framework/bundles" );
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( request.isUserInRole ( "ADMIN" ) )
        {
            result.add ( new MenuEntry ( "System", Integer.MAX_VALUE, "OSGi Framework", 1_000, LinkTarget.createFromController ( FrameworkInformationController.class, "view" ), Modifier.DEFAULT, "cog" ) );
        }

        return result;
    }
}
