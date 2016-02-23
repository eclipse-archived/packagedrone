/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.deb.web;

import static org.eclipse.packagedrone.repo.channel.util.RepositoryLinks.fillRepoLinks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.adapter.deb.ChannelConfiguration;
import org.eclipse.packagedrone.repo.adapter.deb.aspect.AptChannelAspectFactory;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.signing.web.SigningServiceEntry;
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
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.form.FormData;

@Controller
@ViewResolver ( "/WEB-INF/views/config/%s.jsp" )
@Secured
@RequestMapping ( "/config/deb" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    private static final LinkTarget APT_LINK_TEMPLATE = new LinkTarget ( "/apt/{idOrName}" );

    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final List<MenuEntry> result = new LinkedList<> ();
            final ChannelInformation channel = (ChannelInformation)object;

            if ( channel.hasAspect ( AptChannelAspectFactory.ID ) )
            {
                fillRepoLinks ( channel, result, "APT", 5_500, APT_LINK_TEMPLATE );
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

            if ( channel.hasAspect ( AptChannelAspectFactory.ID ) )
            {
                final Map<String, String> model = new HashMap<> ();
                model.put ( "channelId", channel.getId () );

                if ( request.isUserInRole ( "MANAGER" ) )
                {
                    result.add ( new MenuEntry ( "APT", 1_500, LinkTarget.createFromController ( ConfigController.class, "edit" ).expand ( model ), null, null ) );
                }
            }

            return result;
        }

        return null;
    }

    @RequestMapping ( "/channel/{channelId}/edit" )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId) throws Exception
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );
            model.put ( "signingServices", SigningServiceEntry.getSigningServices () );

            final ChannelConfiguration dist = new ChannelConfiguration ();

            dist.setDistribution ( "default" );
            dist.setDefaultComponent ( "main" );
            dist.getArchitectures ().add ( "i386" );
            dist.getArchitectures ().add ( "amd64" );

            MetaKeys.bind ( dist, channel.getMetaData () );

            model.put ( "command", dist );

            return new ModelAndView ( "edit", model );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final ChannelConfiguration cfg, final BindingResult result) throws Exception
    {
        return Channels.withChannel ( this.service, channelId, ModifiableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ( 2 );

            if ( !result.hasErrors () )
            {
                final Map<MetaKey, String> md = MetaKeys.unbind ( cfg );
                channel.applyMetaData ( md );
            }

            model.put ( "channel", channel.getInformation () );
            model.put ( "signingServices", SigningServiceEntry.getSigningServices () );

            return new ModelAndView ( "edit", model );
        } );
    }

}
