/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Bachmann electronic GmbH - #76 Changing role from ADMIN to MANAGER
 *******************************************************************************/
package org.eclipse.packagedrone.repo.trigger.web;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.util.stream.Collectors.toList;
import static org.eclipse.packagedrone.web.ModelAndView.referer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerFactoryInformation;
import org.eclipse.packagedrone.repo.trigger.ConfiguredTriggerFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactoryInformation;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactoryTracker;
import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;
import org.eclipse.packagedrone.repo.trigger.TriggerHandle;
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;
import org.eclipse.packagedrone.repo.web.utils.ChannelServiceController;
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
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

@Controller
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/trigger" )
public class TriggerController extends ChannelServiceController implements InterfaceExtender
{
    private ProcessorFactoryTracker processorFactoryTracker;

    private ConfiguredTriggerFactoryTracker configuredTriggerFactoryTracker;

    public void start ()
    {
        final BundleContext context = FrameworkUtil.getBundle ( TriggerController.class ).getBundleContext ();
        this.processorFactoryTracker = new ProcessorFactoryTracker ( context );
        this.configuredTriggerFactoryTracker = new ConfiguredTriggerFactoryTracker ( context );
    }

    public void stop ()
    {
        if ( this.processorFactoryTracker != null )
        {
            this.processorFactoryTracker.close ();
            this.processorFactoryTracker = null;
        }
        if ( this.configuredTriggerFactoryTracker != null )
        {
            this.configuredTriggerFactoryTracker.close ();
            this.configuredTriggerFactoryTracker = null;
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/list" )
    public ModelAndView list ( @PathVariable ( "channelId" ) final String channelId )
    {
        return withChannel ( channelId, TriggeredChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ();

            final Breadcrumbs.Builder builder = new Breadcrumbs.Builder ();
            builder.add ( "Home", "/" );
            builder.add ( "Channel", "/channel/{0}/view", channel.getId ().getId () );
            builder.add ( "Triggers" );
            builder.buildTo ( model );

            model.put ( "channel", channel.getId () );
            model.put ( "triggers", makeEntries ( channel.listTriggers () ) );
            model.put ( "triggerFactories", makeTriggerFactories () );

            model.put ( "triggerFactoryTracker", (Function<String, ConfiguredTriggerFactoryInformation>) ( factoryId ) -> this.configuredTriggerFactoryTracker.getFactoryInformation ( factoryId ).orElse ( null ) );
            model.put ( "processorFactoryTracker", (Function<String, ProcessorFactoryInformation>) ( factoryId ) -> this.processorFactoryTracker.getFactoryInformation ( factoryId ).orElse ( null ) );

            return new ModelAndView ( "channel/list", model );
        } );
    }

    private List<ConfiguredTriggerFactoryInformation> makeTriggerFactories ()
    {
        return this.configuredTriggerFactoryTracker.getFactoryInformations ();
    }

    @RequestMapping ( value = "/channel/{channelId}/removeProcessor", method = RequestMethod.POST )
    public ModelAndView removeProcessor ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "triggerId" ) final String triggerId, @RequestParameter ( "processorId" ) final String processorId )
    {
        return withChannel ( channelId, TriggeredChannel.class, channel -> {
            channel.deleteProcessor ( triggerId, processorId );
            return referer ( String.format ( "/channel/%s", urlPathSegmentEscaper ().escape ( channelId ) ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeTrigger", method = RequestMethod.POST )
    public ModelAndView removeTrigger ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "triggerId" ) final String triggerId )
    {
        return withChannel ( channelId, TriggeredChannel.class, channel -> {
            channel.deleteConfiguredTrigger ( triggerId );
            return referer ( String.format ( "/channel/%s", urlPathSegmentEscaper ().escape ( channelId ) ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/reorder", method = RequestMethod.POST )
    public void reorder ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "triggerId1" ) final String triggerId1, @RequestParameter ( "processorId1" ) final String processorId1, @RequestParameter ( "triggerId2" ) final String triggerId2, @RequestParameter (
            value = "processorId2",
            required = false ) final String processorId2, final HttpServletResponse response ) throws IOException
    {
        withChannelRun ( channelId, TriggeredChannel.class, channel -> {
            handleFullReorder ( channel, response, triggerId1, processorId1, triggerId2, processorId2 );
        } );
    }

    private void handleFullReorder ( final TriggeredChannel channel, final HttpServletResponse response, final String triggerId1, final String processorId1, final String triggerId2, final String processorId2 ) throws IOException
    {
        response.setContentType ( "text/plain" );

        try
        {
            channel.reorder ( triggerId1, processorId1, triggerId2, processorId2 );
            response.setStatus ( HttpServletResponse.SC_OK );
            response.getWriter ().append ( "OK" );
        }
        catch ( IllegalArgumentException | IllegalStateException e )
        {
            response.sendError ( HttpServletResponse.SC_BAD_REQUEST, ExceptionHelper.getMessage ( e ) );
        }
    }

    private List<TriggerEntry> makeEntries ( final Collection<TriggerHandle> triggers )
    {
        return triggers.stream ().map ( this::makeEntry ).collect ( toList () );
    }

    private TriggerEntry makeEntry ( final TriggerHandle trigger )
    {
        final Optional<TriggerDescriptor> desc = trigger.getDescriptor ();

        List<ProcessorFactoryInformation> processors;
        if ( desc.isPresent () )
        {
            processors = this.processorFactoryTracker.getFactoriesFor ( desc.get ().getSupportedContexts () );
            Collections.sort ( processors, Comparator.comparing ( ProcessorFactoryInformation::getLabel ) );
        }
        else
        {
            processors = Collections.emptyList ();
        }
        return new TriggerEntry ( trigger, processors );
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( ! ( object instanceof ChannelId ) )
        {
            return null;
        }

        if ( !request.isUserInRole ( "MANAGER" ) )
        {
            return null;
        }

        final ChannelId channelId = (ChannelId)object;

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "channelId", channelId.getId () );

        final List<MenuEntry> result = new LinkedList<> ();

        result.add ( new MenuEntry ( "Triggers", 3_300, LinkTarget.createFromController ( TriggerController.class, "list" ).expand ( model ), Modifier.DEFAULT, "cog" ) );

        return result;
    }
}
