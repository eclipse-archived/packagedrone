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
package org.eclipse.packagedrone.repo.channel.web.description;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.CommonController;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;

@Controller
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class DescriptionController implements InterfaceExtender
{
    private static final MetaKey KEY_DESCRIPTION = new MetaKey ( "sys", "description" );

    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/channel/{channelId}/description" )
    public ModelAndView channelDescription ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            return this.service.accessCall ( By.id ( channelId ), ReadableChannel.class, channel -> {
                final Map<String, Object> model = new HashMap<> ( 1 );
                model.put ( "channel", channel.getInformation () );
                return new ModelAndView ( "description", model );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/description", method = RequestMethod.POST )
    public ModelAndView channelDescriptionPost ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "data" ) final String data)
    {
        try
        {
            return this.service.accessCall ( By.id ( channelId ), ModifiableChannel.class, channel -> {

                channel.applyMetaData ( Collections.singletonMap ( KEY_DESCRIPTION, data ) );

                final Map<String, Object> model = new HashMap<> ( 1 );
                model.put ( "channel", channel.getInformation () );
                return new ModelAndView ( "description", model );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    // interface extensions

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( ! ( object instanceof ChannelId ) )
        {
            return null;
        }

        final ChannelId channel = (ChannelId)object;

        final List<MenuEntry> result = new LinkedList<> ();

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "channelId", channel.getId () );

        result.add ( new MenuEntry ( "Help", 100_000, "Description", 1_000, LinkTarget.createFromController ( DescriptionController.class, "channelDescription" ).expand ( model ), Modifier.DEFAULT, "comment" ) );

        return result;
    }
}
