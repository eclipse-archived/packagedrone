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
package org.eclipse.packagedrone.repo.adapter.deb.web;

import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.packagedrone.repo.signing.SigningService;
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
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.form.FormData;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@Controller
@ViewResolver ( "/WEB-INF/views/config/%s.jsp" )
@Secured
@RequestMapping ( "/config/deb" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
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
                final Map<String, String> model = new HashMap<> ();
                model.put ( "channelId", channel.getId () );

                result.add ( new MenuEntry ( "APT Repository", 1_500, new LinkTarget ( "/apt/" + channel.getId () ), Modifier.LINK, null ) );
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

    public static class SigningServiceEntry implements Comparable<SigningServiceEntry>
    {
        private final String id;

        private final String label;

        public SigningServiceEntry ( final String id, final String label )
        {
            this.id = id;
            this.label = label == null ? id : label;
        }

        public String getId ()
        {
            return this.id;
        }

        public String getLabel ()
        {
            return this.label;
        }

        @Override
        public int compareTo ( final SigningServiceEntry o )
        {
            return this.label.compareTo ( o.label );
        }

        @Override
        public String toString ()
        {
            return String.format ( "%s (%s)", this.label, this.id );
        }
    }

    @RequestMapping ( "/channel/{channelId}/edit" )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId) throws Exception
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );
            model.put ( "signingServices", getSigningServices () );

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
            model.put ( "signingServices", getSigningServices () );

            return new ModelAndView ( "edit", model );

        } );
    }

    private List<SigningServiceEntry> getSigningServices ()
    {
        final List<SigningServiceEntry> result = new LinkedList<> ();

        final BundleContext ctx = FrameworkUtil.getBundle ( ConfigController.class ).getBundleContext ();
        Collection<ServiceReference<SigningService>> refs;
        try
        {
            refs = ctx.getServiceReferences ( SigningService.class, null );
        }
        catch ( final InvalidSyntaxException e )
        {
            return Collections.emptyList ();
        }

        if ( refs != null )
        {
            for ( final ServiceReference<SigningService> ref : refs )
            {
                final String pid = makeString ( ref.getProperty ( Constants.SERVICE_PID ) );
                final String description = makeString ( ref.getProperty ( Constants.SERVICE_DESCRIPTION ) );
                result.add ( new SigningServiceEntry ( pid, description ) );
            }
        }

        return result;
    }

    private String makeString ( final Object property )
    {
        if ( property instanceof String )
        {
            return (String)property;
        }
        return null;
    }
}
