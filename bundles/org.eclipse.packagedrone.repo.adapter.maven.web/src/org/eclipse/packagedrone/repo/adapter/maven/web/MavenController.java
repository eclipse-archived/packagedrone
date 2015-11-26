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
package org.eclipse.packagedrone.repo.adapter.maven.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.repo.web.utils.Channels;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class MavenController implements InterfaceExtender
{
    private ChannelService service;

    private SitePrefixService sitePrefixService;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    public void setSitePrefixService ( final SitePrefixService sitePrefixService )
    {
        this.sitePrefixService = sitePrefixService;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            final ChannelInformation channel = (ChannelInformation)object;
            if ( channel.hasAspect ( "maven.repo" ) )
            {
                result.add ( new MenuEntry ( "Maven Repository", 20_000, new LinkTarget ( "/maven/" + channel.getId () ), Modifier.LINK, null ) );
            }

            return result;
        }

        return null;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            final ChannelInformation channel = (ChannelInformation)object;
            final Map<String, String> model = new HashMap<> ( 1 );
            model.put ( "channelId", channel.getId () );
            result.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "Maven", 2_000, LinkTarget.createFromController ( MavenController.class, "help" ).expand ( model ), Modifier.LINK, "info-sign" ) );

            return result;
        }

        return null;
    }

    @RequestMapping ( "/channel/{channelId}/help.maven" )
    public ModelAndView help ( @PathVariable ( "channelId" ) final String channelId)
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            model.put ( "mavenRepo", channel.hasAspect ( "maven.repo" ) );
            model.put ( "channel", channel.getInformation () );
            model.put ( "deployGroups", this.service.getChannelDeployGroups ( By.id ( channelId ) ).orElse ( Collections.emptyList () ) );
            model.put ( "sitePrefix", this.sitePrefixService.getSitePrefix () );

            return new ModelAndView ( "helpMaven", model );
        } );
    }
}
