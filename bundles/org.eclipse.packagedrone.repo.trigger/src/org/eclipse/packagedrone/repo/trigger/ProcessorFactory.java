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
package org.eclipse.packagedrone.repo.trigger;

import java.io.PrintWriter;
import java.io.StringWriter;

public interface ProcessorFactory
{
    public Class<?>[] getSupportedContexts ();

    public default boolean supportsContext ( final Class<?> contextClass )
    {
        if ( contextClass == null )
        {
            return false;
        }

        for ( final Class<?> clazz : getSupportedContexts () )
        {
            if ( clazz.isAssignableFrom ( contextClass ) )
            {
                return true;
            }
        }

        return false;
    }

    public Processor create ( String configuration );

    public default TriggerProcessorState validate ( final String configuration )
    {
        try
        {
            final Processor p = create ( configuration );
            final StringWriter sw = new StringWriter ();
            p.streamHtmlState ( new PrintWriter ( sw ) );
            return new TriggerProcessorState ( sw.toString (), getSupportedContexts () );
        }
        catch ( final Exception e )
        {
            return new TriggerProcessorState ( e );
        }
    }

    public String getLabel ();

    public String getDescription ();

    public String getConfigurationUrl ();
}
