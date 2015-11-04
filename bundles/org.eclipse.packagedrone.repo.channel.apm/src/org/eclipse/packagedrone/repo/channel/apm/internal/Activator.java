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
package org.eclipse.packagedrone.repo.channel.apm.internal;

import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.generator.GeneratorProcessor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{

    private static Activator INSTANCE;

    private ChannelAspectProcessor processor;

    private GeneratorProcessor generatorProcessor;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        INSTANCE = this;

        this.processor = new ChannelAspectProcessor ( context );

        this.generatorProcessor = new GeneratorProcessor ( context );
        this.generatorProcessor.open ();
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        INSTANCE = null;

        if ( this.processor != null )
        {
            this.processor.close ();
            this.processor = null;
        }

        if ( this.generatorProcessor != null )
        {
            this.generatorProcessor.close ();
            this.generatorProcessor = null;
        }
    }

    public static ChannelAspectProcessor getProcessor ()
    {
        return INSTANCE.processor;
    }

    public static GeneratorProcessor getGeneratorProcessor ()
    {
        return INSTANCE.generatorProcessor;
    }

}
