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
package org.eclipse.packagedrone.repo.trigger.web;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.util.stream.Collectors.toList;
import static org.eclipse.packagedrone.web.ModelAndView.referer;

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

import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs;
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
import org.osgi.framework.FrameworkUtil;

@Controller
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/trigger" )
public class TriggerController extends ChannelServiceController implements InterfaceExtender
{
    private ProcessorFactoryTracker processorFactoryTracker;

    public void start ()
    {
        this.processorFactoryTracker = new ProcessorFactoryTracker ( FrameworkUtil.getBundle ( TriggerController.class ).getBundleContext () );
    }

    public void stop ()
    {
        if ( this.processorFactoryTracker != null )
        {
            this.processorFactoryTracker.close ();
            this.processorFactoryTracker = null;
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/list" )
    public ModelAndView list ( @PathVariable ( "channelId" ) final String channelId)
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
            model.put ( "processorFactoryTracker", (Function<String, ProcessorFactoryInformation>) ( factoryId ) -> this.processorFactoryTracker.getFactoryInformation ( factoryId ).orElse ( null ) );

            return new ModelAndView ( "channel/list", model );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeProcessor", method = RequestMethod.POST )
    public ModelAndView removeProcessor ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "triggerId" ) final String triggerId, @RequestParameter ( "processorId" ) final String processorId)
    {
        return withChannel ( channelId, TriggeredChannel.class, channel -> {
            channel.deleteProcessor ( triggerId, processorId );
            return referer ( String.format ( "/channel/%s", urlPathSegmentEscaper ().escape ( channelId ) ) );
        } );
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
