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

import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.manage.system.ConfigurationBackupService;
import org.eclipse.packagedrone.sec.UserInformation;
import org.eclipse.packagedrone.sec.service.LoginException;
import org.eclipse.packagedrone.sec.service.SecurityService;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.CommonController;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.util.BasicAuthentication;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@ViewResolver ( "/WEB-INF/views/backup/%s.jsp" )
@RequestMapping ( "/system/backup" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class BackupController implements InterfaceExtender
{

    private final static Logger logger = LoggerFactory.getLogger ( BackupController.class );

    private ConfigurationBackupService service;

    private SecurityService securityService;

    public void setService ( final ConfigurationBackupService service )
    {
        this.service = service;
    }

    public void setSecurityService ( final SecurityService securityService )
    {
        this.securityService = securityService;
    }

    @RequestMapping
    public ModelAndView main ()
    {
        final Map<String, Object> model = new HashMap<> ( 1 );
        return new ModelAndView ( "index", model );
    }

    @RequestMapping ( "/export" )
    public void exportData ( final HttpServletResponse response ) throws IOException
    {
        response.setContentType ( "application/zip" );
        response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=package-drone-backup-%1$tY%1$tm%1$td-%1$tH-%1$tM.zip", new Date () ) );
        response.setStatus ( HttpServletResponse.SC_OK );
        this.service.createConfigurationBackup ( response.getOutputStream () );
    }

    @RequestMapping ( value = "/provision", method = RequestMethod.POST )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public void provision ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        internalProvision ( request, response );
    }

    protected void internalProvision ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        final String[] authToks = BasicAuthentication.parseAuthorization ( request );
        if ( authToks == null )
        {
            BasicAuthentication.request ( response, "provision", "Please authenticate" );
            return;
        }

        UserInformation user;
        try
        {
            user = this.securityService.login ( authToks[0], authToks[1] );
            if ( user == null )
            {
                quickResponse ( response, HttpServletResponse.SC_FORBIDDEN, "Not allowed" );
                return;
            }
        }
        catch ( final LoginException e )
        {
            quickResponse ( response, HttpServletResponse.SC_FORBIDDEN, "Not allowed" );
            return;
        }

        try
        {
            this.service.provisionConfiguration ( request.getInputStream () );

            waitForService ();

            quickResponse ( response, HttpServletResponse.SC_OK, "OK" );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to import configuration", e );
            quickResponse ( response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to import configuration" );
            return;
        }
    }

    @RequestMapping ( value = "/import", method = RequestMethod.POST )
    public ModelAndView restoreData ( @RequestParameter ( "file" ) final Part part)
    {
        try
        {
            this.service.restoreConfiguration ( part.getInputStream () );
            waitForService ();
            return new ModelAndView ( "redirect:/system/backup" );
        }
        catch ( final Exception e )
        {
            // we require ADMIN permissions, so we can show the stack trace
            return CommonController.createError ( "Restore", "Failed to restore configuration", e, true );
        }
    }

    private void waitForService ()
    {
        final ServiceTracker<?, ?> tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( BackupController.class ).getBundleContext (), ChannelService.class, null );
        tracker.open ();
        try
        {
            tracker.waitForService ( 5_000 ); // wait 5 seconds
        }
        catch ( final InterruptedException e )
        {
        }
        finally
        {
            tracker.close ();
        }
    }

    protected void quickResponse ( final HttpServletResponse response, final int statusCode, final String message ) throws IOException
    {
        response.setStatus ( statusCode );
        response.setContentType ( "text/plain" );
        response.getWriter ().write ( message );
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        if ( request.isUserInRole ( "ADMIN" ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            result.add ( new MenuEntry ( "System", 20_000, "Configuration", 200, LinkTarget.createFromController ( BackupController.class, "main" ), null, null ) );

            return result;
        }

        return null;
    }
}
