/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.web.artifact;

import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;
import static org.eclipse.packagedrone.repo.channel.web.internal.Activator.getGeneratorProcessor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.channel.ChannelArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.web.channel.ChannelController;
import org.eclipse.packagedrone.repo.web.utils.Channels;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.sec.web.filter.SecurityFilter;
import org.eclipse.packagedrone.utils.Holder;
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
import org.eclipse.packagedrone.web.controller.binding.PathVariable;

import com.google.common.net.UrlEscapers;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ArtifactController implements InterfaceExtender
{
    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/generate", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView generate ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId)
    {
        return Channels.withArtifact ( this.service, channelId, artifactId, ModifiableChannel.class, ( channel, artifact ) -> {
            channel.getContext ().regenerate ( artifact.getId () );
            return new ModelAndView ( "redirect:/channel/" + UrlEscapers.urlPathSegmentEscaper ().escape ( artifact.getChannelId ().getId () ) + "/view" );
        } );
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof org.eclipse.packagedrone.repo.channel.ArtifactInformation )
        {
            final ChannelArtifactInformation ai = (ChannelArtifactInformation)object;

            final List<MenuEntry> result = new LinkedList<> ();

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", ai.getChannelId ().getId () );
            model.put ( "artifactId", ai.getId () );

            result.add ( new MenuEntry ( "Channel", 100, LinkTarget.createFromController ( ChannelController.class, "view" ).expand ( model ), Modifier.DEFAULT, null ) );

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                if ( ai.is ( "generator" ) )
                {
                    final Holder<LinkTarget> holder = new Holder<> ();
                    getGeneratorProcessor ().process ( ai.getVirtualizerAspectId (), generator -> {
                        holder.value = generator.getEditTarget ( ai );
                    } );

                    if ( holder.value != null )
                    {
                        result.add ( new MenuEntry ( "Edit", 400, holder.value, Modifier.DEFAULT, null ) );
                    }
                }
            }

            if ( SecurityFilter.isLoggedIn ( request ) )
            {
                if ( ai.is ( "generator" ) )
                {
                    result.add ( new MenuEntry ( "Regenerate", 300, LinkTarget.createFromController ( ArtifactController.class, "generate" ).expand ( model ), Modifier.SUCCESS, "refresh" ) );
                }
            }

            result.add ( new MenuEntry ( "Download", Integer.MAX_VALUE, LinkTarget.createFromController ( ChannelController.class, "getArtifact" ).expand ( model ), Modifier.LINK, "download" ) );
            result.add ( new MenuEntry ( "View", Integer.MAX_VALUE, LinkTarget.createFromController ( ChannelController.class, "dumpArtifact" ).expand ( model ), Modifier.LINK, null ) );

            return result;
        }
        return null;
    }

}
