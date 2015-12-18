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
package org.eclipse.packagedrone.repo.adapter.rpm.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.repo.signing.web.SigningServiceEntry;
import org.eclipse.packagedrone.repo.web.utils.Channels;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.form.FormData;

@Controller
@RequestMapping ( value = "/ui/yum" )
@ViewResolver ( "/WEB-INF/views/yum/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class YumController
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

    @RequestMapping ( value = "/{channelId}/help" )
    public ModelAndView help ( @PathVariable ( "channelId" ) final String channelId)
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {
            final ModelAndView model = new ModelAndView ( "help" );

            model.put ( "channel", channel.getInformation () );
            model.put ( "sitePrefix", this.sitePrefixService.getSitePrefix () );

            return model;
        } );
    }

    @RequestMapping ( value = "/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String id)
    {
        return Channels.withChannel ( this.service, id, ReadableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            final YumConfiguration cfg = new YumConfiguration ();

            MetaKeys.bind ( cfg, channel.getContext ().getProvidedMetaData () );

            model.put ( "channel", channel.getInformation () );
            model.put ( "command", cfg );
            model.put ( "signingServices", SigningServiceEntry.getSigningServices () );

            return new ModelAndView ( "edit", model );
        } );
    }

    @RequestMapping ( value = "/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String id, @FormData ( "command" ) @Valid final YumConfiguration command, final BindingResult result)
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "signingServices", SigningServiceEntry.getSigningServices () );

        if ( !result.hasErrors () )
        {
            return Channels.withChannel ( this.service, id, ModifiableChannel.class, channel -> {

                final Map<MetaKey, String> metadata = MetaKeys.unbind ( command );
                channel.getContext ().applyMetaData ( metadata );

                model.put ( "channel", channel.getInformation () );

                return new ModelAndView ( "redirect:edit" );
            } );
        }
        else
        {
            return Channels.withChannel ( this.service, id, ReadableChannel.class, channel -> {
                model.put ( "channel", channel.getInformation () );
                return new ModelAndView ( "edit", model );
            } );
        }
    }
}
