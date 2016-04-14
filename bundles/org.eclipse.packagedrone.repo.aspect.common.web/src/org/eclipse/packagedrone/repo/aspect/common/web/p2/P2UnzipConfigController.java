/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.web.p2;

import static org.eclipse.packagedrone.repo.web.CommonCategories.EDIT;
import static org.eclipse.packagedrone.web.LinkTarget.createFromController;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs.Entry;
import org.eclipse.packagedrone.repo.web.utils.ChannelServiceController;
import org.eclipse.packagedrone.repo.web.utils.Channels;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
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
@RequestMapping ( "/aspect/p2.unzip/{channelId}" )
@ViewResolver ( "/WEB-INF/views/p2.unzip/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class P2UnzipConfigController extends ChannelServiceController implements InterfaceExtender
{
    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( !request.isUserInRole ( "MANAGER" ) )
        {
            return null;
        }

        if ( ! ( object instanceof ChannelInformation ) )
        {
            return null;
        }

        final List<MenuEntry> result = new LinkedList<> ();

        final Map<String, Object> model = Collections.singletonMap ( "channelId", ( (ChannelInformation)object ).getId () );

        result.add ( new MenuEntry ( "Edit", EDIT.getPriority (), "P2 Unzipper", 10_000, createFromController ( P2UnzipConfigController.class, "edit" ).expand ( model ), null, null ) );

        return result;
    }

    @RequestMapping ( value = "/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId )
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            final Configuration cfg = MetaKeys.bind ( new Configuration (), channel.getContext ().getProvidedMetaData () );

            model.put ( "command", cfg );
            fillCommonViewData ( channel, model );

            return new ModelAndView ( "edit", model );
        } );
    }

    @RequestMapping ( value = "/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @FormData ( "command" ) final Configuration cfg, final BindingResult result )
    {
        return withChannel ( channelId, ModifiableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            model.put ( "command", cfg );
            fillCommonViewData ( channel, model );

            if ( !result.hasErrors () )
            {
                channel.applyMetaData ( MetaKeys.unbind ( cfg ) );
            }

            return new ModelAndView ( "edit", model );
        } );
    }

    private void fillCommonViewData ( final ReadableChannel channel, final Map<String, Object> model )
    {
        model.put ( "channel", channel.getId () );
        fillBreadcrumbs ( model, channel.getId ().getId (), "Edit" );
    }

    private void fillBreadcrumbs ( final Map<String, Object> model, final String channelId, final String action )
    {
        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), new Entry ( "Channel", Channels.channelTarget ( channelId ) ), new Entry ( action ) ) );
    }
}
