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

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@ViewResolver ( "/WEB-INF/views/info/%s.jsp" )
@RequestMapping ( "/system/info" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class InformationController implements InterfaceExtender
{
    private final static Logger logger = LoggerFactory.getLogger ( InformationController.class );

    private StorageManager manager;

    public void setManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void unsetManager ( final StorageManager manager )
    {
        this.manager = null;
    }

    @RequestMapping
    public ModelAndView view ()
    {
        final Map<String, Object> model = new HashMap<> ();

        final Runtime r = Runtime.getRuntime ();

        // runtime

        model.put ( "freeMemory", r.freeMemory () );
        model.put ( "maxMemory", r.maxMemory () );
        model.put ( "totalMemory", r.totalMemory () );
        model.put ( "usedMemory", r.totalMemory () - r.freeMemory () );

        // system

        model.put ( "availableProcessors", r.availableProcessors () );

        // java

        model.put ( "java", makeJavaInformation () );

        // storage

        fillFromStorage ( model );

        return new ModelAndView ( "index", model );
    }

    private void fillFromStorage ( final Map<String, Object> model )
    {
        if ( this.manager != null )
        {
            final Path base = this.manager.getContext ().getBasePath ();
            try
            {
                final FileStore store = Files.getFileStore ( base );
                model.put ( "storageTotal", store.getTotalSpace () );
                model.put ( "storageFree", store.getUsableSpace () );
                model.put ( "storageUsed", store.getTotalSpace () - store.getUsableSpace () );
                model.put ( "storageName", store.name () );
            }
            catch ( final Exception e )
            {
                logger.warn ( "Failed to check storage space", e );
                // ignore
            }
        }
    }

    private Map<String, String> makeJavaInformation ()
    {
        final Map<String, String> result = new LinkedHashMap<> ();

        result.put ( "Version", System.getProperty ( "java.version" ) );
        result.put ( "Vendor", System.getProperty ( "java.vendor" ) );
        result.put ( "Temp Dir", System.getProperty ( "java.io.tmpdir" ) );
        result.put ( "Home", System.getProperty ( "java.home" ) );

        return result;
    }

    @RequestMapping ( "/gc" )
    public String gc ()
    {
        System.gc ();
        return "referer:/system/info";
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( request.isUserInRole ( "ADMIN" ) )
        {
            result.add ( new MenuEntry ( "System", Integer.MAX_VALUE, "Information", 1_000, LinkTarget.createFromController ( InformationController.class, "view" ), Modifier.DEFAULT, "cog" ) );
        }

        return result;
    }
}
