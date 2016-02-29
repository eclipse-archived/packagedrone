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
package org.eclipse.packagedrone.repo.trigger.cleanup.internal;

import static org.eclipse.packagedrone.repo.cleanup.web.TestConfigurationController.performTest;

import javax.servlet.annotation.HttpConstraint;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.cleanup.Aggregator;
import org.eclipse.packagedrone.repo.cleanup.Field;
import org.eclipse.packagedrone.repo.cleanup.Order;
import org.eclipse.packagedrone.repo.cleanup.Sorter;
import org.eclipse.packagedrone.repo.trigger.cleanup.CleanupConfiguration;
import org.eclipse.packagedrone.repo.trigger.common.RequestParameterBasedConfigurationController;
import org.eclipse.packagedrone.repo.trigger.common.SimpleProcessorConfigurationController;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.CommonController;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/trigger/processor.factory/cleanup/configure" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class CleanupConfigurationController extends SimpleProcessorConfigurationController<CleanupConfiguration> implements RequestParameterBasedConfigurationController<CleanupConfiguration>
{
    public CleanupConfigurationController ()
    {
        super ( CleanupProcessorFactory.ID, "configuration" );
    }

    @Override
    protected CleanupConfiguration newModel ()
    {
        final CleanupConfiguration result = new CleanupConfiguration ();

        final Aggregator aggregator = new Aggregator ();
        aggregator.getFields ().add ( new MetaKey ( "mvn", "groupId" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "artifactId" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "version" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "extension" ) );

        final Sorter sorter = new Sorter ();
        sorter.getFields ().add ( new Field ( new MetaKey ( "mvn", "snapshotVersion" ), Order.ASCENDING ) );

        result.setAggregator ( aggregator );
        result.setSorter ( sorter );

        result.setNumberOfEntries ( 3 );
        result.setIgnoreWhenMissingFields ( true );
        result.setRootOnly ( true );

        return result;
    }

    @Override
    protected CleanupConfiguration parseModel ( final String configuration )
    {
        return CleanupConfiguration.valueOf ( configuration );
    }

    @Override
    protected String writeModel ( final CleanupConfiguration configuration )
    {
        return configuration.toJson ();
    }

    @RequestMapping ( value = "/test", method = RequestMethod.POST )
    public ModelAndView testJsonPost ( @RequestParameter ( "channelId" ) final String channelId, @RequestParameter ( "configuration" ) final String json)
    {
        final CleanupConfiguration cfg;
        try
        {
            cfg = CleanupConfiguration.valueOf ( json );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Error", "Failed to parse configuration", e );
        }

        return withChannel ( channelId, ReadableChannel.class, channel -> performTest ( channel, cfg::applyTo ) );
    }

}
