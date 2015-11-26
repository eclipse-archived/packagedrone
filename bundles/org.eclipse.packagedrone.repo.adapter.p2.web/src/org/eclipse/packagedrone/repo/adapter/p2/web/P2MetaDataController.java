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
package org.eclipse.packagedrone.repo.adapter.p2.web;

import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2MetaDataInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs.Entry;
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

import com.google.common.net.UrlEscapers;

@Controller
@RequestMapping ( value = "/p2.metadata" )
@ViewResolver ( "/WEB-INF/views/metadata/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class P2MetaDataController
{
    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/{channelId}/info" )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public ModelAndView info ( @PathVariable ( "channelId" ) final String channelId) throws Exception
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ();

            final Map<MetaKey, String> metaData = channel.getMetaData ();

            final P2MetaDataInformation channelInfo = new P2MetaDataInformation ();
            MetaKeys.bind ( channelInfo, metaData );

            model.put ( "channel", channel.getInformation () );
            model.put ( "channelInfo", channelInfo );

            return new ModelAndView ( "p2info", model );
        } );

    }

    @RequestMapping ( value = "/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId) throws Exception
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ();

            final Map<MetaKey, String> metaData = channel.getContext ().getProvidedMetaData ();

            final P2MetaDataInformation channelInfo = new P2MetaDataInformation ();

            MetaKeys.bind ( channelInfo, metaData );

            model.put ( "channel", channel.getInformation () );
            model.put ( "command", channelInfo );

            fillBreadcrumbs ( model, channel.getId ().getId (), "Edit" );

            return new ModelAndView ( "p2edit", model );
        } );
    }

    private void fillBreadcrumbs ( final Map<String, Object> model, final String channelId, final String action )
    {
        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), new Entry ( "Channel", Channels.channelTarget ( channelId ) ), new Entry ( action ) ) );
    }

    @RequestMapping ( value = "/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final P2MetaDataInformation data, final BindingResult result) throws Exception
    {
        return Channels.withChannel ( this.service, channelId, ModifiableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            if ( result.hasErrors () )
            {
                model.put ( "channel", channel.getInformation () );
                model.put ( "command", data );
                fillBreadcrumbs ( model, channelId, "Edit" );
                return new ModelAndView ( "p2edit", model );
            }

            final Map<MetaKey, String> providedMetaData = MetaKeys.unbind ( data );

            channel.applyMetaData ( providedMetaData );

            return new ModelAndView ( "redirect:/p2.metadata/" + UrlEscapers.urlPathSegmentEscaper ().escape ( channelId ) + "/info", model );
        } );
    }
}
