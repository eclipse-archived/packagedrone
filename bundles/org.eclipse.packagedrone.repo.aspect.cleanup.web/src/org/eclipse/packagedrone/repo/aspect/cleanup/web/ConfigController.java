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
package org.eclipse.packagedrone.repo.aspect.cleanup.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.aspect.cleanup.Aggregator;
import org.eclipse.packagedrone.repo.aspect.cleanup.CleanupConfiguration;
import org.eclipse.packagedrone.repo.aspect.cleanup.CleanupTester;
import org.eclipse.packagedrone.repo.aspect.cleanup.Field;
import org.eclipse.packagedrone.repo.aspect.cleanup.Sorter;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
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
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.CommonController;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.controller.form.FormData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.UrlEscapers;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping ( "/aspect/cleanup/{channelId}/config" )
@ViewResolver ( "/WEB-INF/views/config/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    private final static Logger logger = LoggerFactory.getLogger ( ConfigController.class );

    private CleanupTester tester;

    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    public void setTester ( final CleanupTester tester )
    {
        this.tester = tester;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( object instanceof ChannelInformation )
        {
            final ChannelInformation channel = (ChannelInformation)object;
            if ( channel.hasAspect ( "cleanup" ) && request.isUserInRole ( "MANAGER" ) )
            {
                final Map<String, String> model = new HashMap<> ();
                model.put ( "channelId", channel.getId () );
                result.add ( new MenuEntry ( "Cleanup", 7_000, LinkTarget.createFromController ( ConfigController.class, "edit" ).expand ( model ), null, null ) );
            }
        }

        return result;
    }

    @RequestMapping ( "/edit" )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter (
            value = "configuration", required = false ) final String configString)
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ( 2 );

            model.put ( "channel", channel.getInformation () );

            CleanupConfiguration cfg;

            try
            {
                if ( configString != null && !configString.isEmpty () )
                {
                    // use the content from the input parameter
                    cfg = new GsonBuilder ().create ().fromJson ( configString, CleanupConfiguration.class );
                }
                else
                {
                    cfg = MetaKeys.bind ( makeDefaultConfiguration (), channel.getMetaData () );
                }
            }
            catch ( final Exception e )
            {
                logger.info ( "Failed to parse cleanup config", e );
                // something failed, go back to default
                cfg = makeDefaultConfiguration ();
            }

            model.put ( "command", cfg );

            fillModel ( model, channelId );

            return new ModelAndView ( "edit", model );
        } );
    }

    protected CleanupConfiguration makeDefaultConfiguration ()
    {
        CleanupConfiguration cfg;

        cfg = new CleanupConfiguration ();

        cfg.setNumberOfVersions ( 3 );
        cfg.setOnlyRootArtifacts ( true );

        final Aggregator aggregator = new Aggregator ();
        aggregator.getFields ().add ( new MetaKey ( "mvn", "groupId" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "artifactId" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "version" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "classifier" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "extension" ) );
        cfg.setAggregator ( aggregator );

        final Sorter sorter = new Sorter ();
        sorter.getFields ().add ( new Field ( "mvn", "snapshotVersion" ) );
        cfg.setSorter ( sorter );
        return cfg;
    }

    @RequestMapping ( value = "/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final CleanupConfiguration cfg, final BindingResult result)
    {
        return Channels.withChannel ( this.service, channelId, ModifiableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ( 2 );

            model.put ( "command", cfg );
            model.put ( "channel", channel.getInformation () );
            fillModel ( model, channelId );

            try
            {
                if ( !result.hasErrors () )
                {
                    channel.applyMetaData ( MetaKeys.unbind ( cfg ) );
                }
            }
            catch ( final Exception e )
            {
                return CommonController.createError ( "Update configuration", "Failed to update cleanup configuration", e );
            }

            return new ModelAndView ( "edit", model );

        } );
    }

    @RequestMapping ( value = "/test", method = RequestMethod.POST )
    public ModelAndView testPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final CleanupConfiguration cfg, final BindingResult result)
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {

            if ( !result.hasErrors () )
            {
                final Map<String, Object> model = new HashMap<> ();

                model.put ( "command", cfg );
                model.put ( "channel", channel.getInformation () );
                fillModel ( model, channelId );

                model.put ( "result", this.tester.testCleanup ( channel.getArtifacts (), cfg ) );
                return new ModelAndView ( "testResult", model );
            }
            else
            {
                return CommonController.createError ( "Error", "Testing cleanup", "The configuration has errors" );
            }
        } );
    }

    private void fillModel ( final Map<String, Object> model, final String channelId )
    {
        final List<Entry> entries = new LinkedList<> ();

        entries.add ( new Entry ( "Home", "/" ) );
        entries.add ( new Entry ( "Channel", "/channel/" + UrlEscapers.urlPathSegmentEscaper ().escape ( channelId ) + "/view" ) );
        entries.add ( new Entry ( "Cleanup" ) );

        model.put ( "breadcrumbs", new Breadcrumbs ( entries ) );
    }
}
