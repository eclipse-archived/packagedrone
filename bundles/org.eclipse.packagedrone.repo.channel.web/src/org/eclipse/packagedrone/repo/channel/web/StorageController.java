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
package org.eclipse.packagedrone.repo.channel.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.transfer.TransferService;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.utils.Strings;
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
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.MessageBindingError;
import org.eclipse.packagedrone.web.controller.form.FormData;
import org.eclipse.packagedrone.web.controller.validator.ControllerValidator;
import org.eclipse.packagedrone.web.controller.validator.ValidationContext;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/global/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class StorageController implements InterfaceExtender
{
    private ChannelService service;

    private TransferService transferService;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    public void setTransferService ( final TransferService transferService )
    {
        this.transferService = transferService;
    }

    @RequestMapping ( value = "/system/storage" )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView index ()
    {
        final Map<String, Object> model = new HashMap<> ();
        return new ModelAndView ( "index", model );
    }

    @RequestMapping ( value = "/system/storage/wipe", method = RequestMethod.POST )
    public ModelAndView wipe ()
    {
        this.service.wipeClean ();
        return new ModelAndView ( "redirect:/channel" );
    }

    @HttpConstraint ( rolesAllowed = "ADMIN" )
    @RequestMapping ( value = "/system/storage/exportAllFs", method = RequestMethod.GET )
    public ModelAndView exportAllFs ()
    {
        return new ModelAndView ( "exportAllFs" );
    }

    @HttpConstraint ( rolesAllowed = "ADMIN" )
    @RequestMapping ( value = "/system/storage/exportAllFs", method = RequestMethod.POST )
    public ModelAndView exportAllFsPost ( @Valid @FormData ( "command" ) final ExportAllFileSystemCommand command, final BindingResult result)
    {
        if ( result.hasErrors () )
        {
            return new ModelAndView ( "exportAllFs" );
        }

        File location;
        try
        {
            location = performExport ( command );
        }
        catch ( final IOException e )
        {
            return CommonController.createError ( "Spool out", null, e, true );
        }

        final String bytes = Strings.bytes ( location.length () );

        return CommonController.createSuccess ( "Spool out", "to file system", String.format ( "<strong>Complete!</strong> Successfully spooled out all channels to <code>%s</code> (%s)", location, bytes ) );
    }

    public File performExport ( final ExportAllFileSystemCommand command ) throws IOException
    {
        final File file = new File ( command.getLocation () ).getAbsoluteFile ();

        // fail if the file exists right now
        Files.createFile ( file.toPath () );

        try ( BufferedOutputStream stream = new BufferedOutputStream ( new FileOutputStream ( file ) ) )
        {
            this.transferService.exportAll ( stream );
        }

        return file;
    }

    @ControllerValidator ( formDataClass = ExportAllFileSystemCommand.class )
    public void validateExportAll ( final ExportAllFileSystemCommand command, final ValidationContext ctx )
    {
        final String locationString = command.getLocation ();
        if ( locationString == null || locationString.isEmpty () )
        {
            return;
        }

        final File location = new File ( locationString ).getAbsoluteFile ();
        if ( location.isDirectory () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "'%s' must not be an existing directory", location ) ) );
            return;
        }
        if ( location.exists () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "'%s' must not exist", location ) ) );
            return;
        }
        if ( !location.getParentFile ().isDirectory () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "'%s' must be an existing directory", location.getParentFile () ) ) );
            return;
        }
        if ( !location.getParentFile ().canWrite () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "'%s' must be writable by the server", location.getParentFile () ) ) );
            return;
        }
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( request.isUserInRole ( "MANAGER" ) )
        {
            result.add ( new MenuEntry ( "System", Integer.MAX_VALUE, "Storage", 200, LinkTarget.createFromController ( StorageController.class, "index" ), null, null ) );
        }

        return result;
    }
}
