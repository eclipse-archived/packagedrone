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
package org.eclipse.packagedrone.repo.aspect.mvnosgi.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.web.utils.Channels;
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
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.form.FormData;

@Controller
@RequestMapping ( "/aspect/mvnosgi/{channelId}" )
@ViewResolver ( "/WEB-INF/views/aspect/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class AspectController implements InterfaceExtender
{

    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @RequestMapping ( "/config" )
    public ModelAndView config ( @PathVariable ( "channelId" ) final String channelId)
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );

            final Configuration data = new Configuration ();
            try
            {
                MetaKeys.bind ( data, channel.getMetaData () );
                model.put ( "command", data );
            }
            catch ( final Exception e )
            {
                // ignore data
            }

            return new ModelAndView ( "config", model );

        } );
    }

    @RequestMapping ( value = "/config", method = RequestMethod.POST )
    public ModelAndView configPost ( @PathVariable ( "channelId" ) final String channelId, @FormData ( "command" ) @Valid final Configuration data, final BindingResult result)
    {
        return Channels.withChannel ( this.service, channelId, ModifiableChannel.class, channel -> {
            if ( !result.hasErrors () )
            {
                try
                {
                    channel.applyMetaData ( MetaKeys.unbind ( data ) );
                }
                catch ( final Exception e )
                {
                    return CommonController.createError ( "Error", "Failed to update", e );
                }
            }

            final Map<String, Object> model = new HashMap<> ();
            model.put ( "channel", channel.getInformation () );
            return new ModelAndView ( "config", model );
        } );
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( ! ( object instanceof ChannelInformation ) )
        {
            return null;
        }

        final ChannelInformation channel = (ChannelInformation)object;
        if ( !channel.hasAspect ( "mvnosgi" ) )
        {
            return null;
        }

        if ( !request.isUserInRole ( "MANAGER" ) )
        {
            return null;
        }

        final List<MenuEntry> result = new LinkedList<> ();

        final Map<String, Object> model = Collections.singletonMap ( "channelId", channel.getId () );

        result.add ( new MenuEntry ( "Edit", Integer.MAX_VALUE, "Maven POM Creator", 1_001, LinkTarget.createFromController ( AspectController.class, "config" ).expand ( model ), null, null ) );

        return result;
    }
}
