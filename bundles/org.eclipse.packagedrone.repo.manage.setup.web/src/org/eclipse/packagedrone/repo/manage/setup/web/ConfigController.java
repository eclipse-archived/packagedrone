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
package org.eclipse.packagedrone.repo.manage.setup.web;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.packagedrone.mail.MailService;
import org.eclipse.packagedrone.repo.manage.setup.web.internal.Activator;
import org.eclipse.packagedrone.sec.web.controller.HttpConstraints;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.LinkTarget.ControllerMethod;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.form.FormData;
import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@Controller
@RequestMapping ( value = "/config" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    private static final String SYSPROP_STORAGE_BASE = "drone.storage.base";

    private static final String PID_STORAGE_MANAGER = "drone.storage.manager";

    private final static ControllerMethod METHOD_MAIN = LinkTarget.getControllerMethod ( ConfigController.class, "config" );

    private ConfigurationAdmin configurationAdmin;

    public void setConfigurationAdmin ( final ConfigurationAdmin configAdmin )
    {
        this.configurationAdmin = configAdmin;
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( HttpConstraints.isCallAllowed ( METHOD_MAIN, request ) )
        {
            result.add ( new MenuEntry ( "Administration", 10_000, "Storage Setup", 100, LinkTarget.createFromController ( METHOD_MAIN ), Modifier.DEFAULT, null, false, 0 ) );
        }

        return result;
    }

    private void fillData ( final Map<String, Object> model )
    {
        model.put ( "sysProp", System.getProperty ( SYSPROP_STORAGE_BASE ) );
        model.put ( "freeSpacePercent", freeSpace () );
    }

    private Double freeSpace ()
    {
        try
        {
            final String base = System.getProperty ( SYSPROP_STORAGE_BASE );
            final Path p = Paths.get ( base );

            final FileStore store = Files.getFileStore ( p );
            return (double)store.getUnallocatedSpace () / (double)store.getTotalSpace ();
        }
        catch ( final Exception e )
        {
            return null;
        }
    }

    @RequestMapping
    public ModelAndView config ()
    {
        final Map<String, Object> model = new HashMap<> ();

        final StorageConfiguration command = new StorageConfiguration ();

        try
        {
            final Configuration cfg = this.configurationAdmin.getConfiguration ( PID_STORAGE_MANAGER, null );
            if ( cfg != null && cfg.getProperties () != null )
            {
                command.setBasePath ( (String)cfg.getProperties ().get ( "basePath" ) );
            }
        }
        catch ( final Exception e )
        {
            // ignore
        }

        model.put ( "command", command );
        fillData ( model );

        return new ModelAndView ( "/config/index", model );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView configPost ( @Valid @FormData ( "command" ) final StorageConfiguration data, final BindingResult result)
    {
        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "command", data );

        fillData ( model );

        if ( !result.hasErrors () )
        {
            try
            {
                if ( applyConfiguration ( data.getBasePath () ) )
                {
                    // now wait until the configuration was performed in the background
                    try
                    {
                        Activator.getTracker ().waitForService ( 5000 );
                    }
                    catch ( final InterruptedException e )
                    {
                    }
                }
            }
            catch ( final IOException e )
            {
                model.put ( "error", ExceptionHelper.getMessage ( e ) );
            }
        }

        // either we still have something to do here, or we are fully set up

        if ( isMailServicePresent () || result.hasErrors () )
        {
            return new ModelAndView ( "/config/index", model );
        }
        else
        {
            return new ModelAndView ( "redirect:/setup" );
        }
    }

    private boolean applyConfiguration ( final String basePath ) throws IOException
    {
        final Configuration cfg = this.configurationAdmin.getConfiguration ( PID_STORAGE_MANAGER, null );
        if ( basePath == null || basePath.isEmpty () )
        {
            cfg.update ( new Hashtable<> () );
            return false;
        }
        else
        {
            final Dictionary<String, Object> data = new Hashtable<> ();
            data.put ( "basePath", basePath );
            cfg.update ( data );
            return true;
        }
    }

    protected boolean isMailServicePresent ()
    {
        return FrameworkUtil.getBundle ( ConfigController.class ).getBundleContext ().getServiceReference ( MailService.class ) != null;
    }

}
