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
package org.eclipse.packagedrone.repo.cleanup.web;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.jsp.JspWriter;

import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs.Entry;
import org.eclipse.packagedrone.repo.cleanup.Cleaner;
import org.eclipse.packagedrone.utils.io.IOConsumer;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.ViewResolver;

@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public final class TestConfigurationController
{
    private TestConfigurationController ()
    {
    }

    public static ModelAndView performTest ( final ReadableChannel channel, final Consumer<Cleaner> cleanerConfigurator )
    {
        return performTestCustomized ( channel, cleanerConfigurator, null );
    }

    public static ModelAndView performTestCustomized ( final ReadableChannel channel, final Consumer<Cleaner> cleanerConfigurator, final IOConsumer<JspWriter> customizer )
    {
        final Cleaner cleaner = new Cleaner ( channel::getArtifacts );
        cleanerConfigurator.accept ( cleaner );
        return makeTestResultPage ( channel, cleaner, customizer );
    }

    private static ModelAndView makeTestResultPage ( final ReadableChannel channel, final Cleaner cleaner, final IOConsumer<JspWriter> customizer )
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "channel", channel.getInformation () );
        fillBreadcrumbs ( model, channel.getId ().getId () );
        model.put ( "cleaner", cleaner );
        model.put ( "result", cleaner.compute () );
        model.put ( "customizer", customizer );

        final ModelAndView result = new ModelAndView ( "testResult", model );
        result.setAlternateViewResolver ( TestConfigurationController.class );
        return result;
    }

    private static void fillBreadcrumbs ( final Map<String, Object> model, final String channelId )
    {
        final List<Entry> entries = new LinkedList<> ();

        entries.add ( new Entry ( "Home", "/" ) );
        entries.add ( new Entry ( "Channel", "/channel/" + urlPathSegmentEscaper ().escape ( channelId ) + "/view" ) );
        entries.add ( new Entry ( "Cleanup" ) );

        model.put ( "breadcrumbs", new Breadcrumbs ( entries ) );
    }
}
