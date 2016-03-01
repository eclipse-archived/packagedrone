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
package org.eclipse.packagedrone.repo.trigger.common.foobar;

import java.io.PrintWriter;

import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.trigger.Processor;
import org.eclipse.packagedrone.repo.trigger.ProcessorFactory;
import org.eclipse.packagedrone.web.LinkTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

public class FooBarProcessorFactory implements ProcessorFactory
{
    private static final Class<?>[] SUPPORTED_CONTEXTS = new Class<?>[] { ModifiableChannel.class };

    private final static Logger logger = LoggerFactory.getLogger ( FooBarProcessorFactory.class );

    public static final String ID = "foobar";

    @Override
    public Class<?>[] getSupportedContexts ()
    {
        return SUPPORTED_CONTEXTS;
    }

    @Override
    public Processor create ( final String configuration ) throws IllegalArgumentException
    {
        final FooBarConfiguration cfg = FooBarConfiguration.fromJson ( configuration );

        return new Processor () {

            @Override
            public void process ( final Object context )
            {
                System.out.format ( "Foo bar: %s - %s%n", cfg.getString1 (), context );
            }

            @Override
            public void streamHtmlState ( final PrintWriter writer )
            {
                final Escaper esc = HtmlEscapers.htmlEscaper ();
                writer.format ( "<p>This action is doing foo bar: <code>%s</code></p>", esc.escape ( cfg.getString1 () ) );
            }
        };
    }

    @Override
    public String getLabel ()
    {
        return "Foo bar";
    }

    @Override
    public String getDescription ()
    {
        return "Doing a lot of foo bar";
    }

    @Override
    public String getConfigurationUrl ()
    {
        try
        {
            return LinkTarget.createFromController ( FooBarConfigurationController.class, "configure" ).getUrl ();
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to generate configuration url", e );
            return null;
        }
    }
}
