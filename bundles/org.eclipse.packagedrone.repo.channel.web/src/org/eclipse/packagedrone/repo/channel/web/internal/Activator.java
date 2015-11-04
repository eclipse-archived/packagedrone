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
package org.eclipse.packagedrone.repo.channel.web.internal;

import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.aspect.recipe.RecipeProcessor;
import org.eclipse.packagedrone.repo.generator.GeneratorProcessor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{

    private static Activator INSTANCE;

    private ChannelAspectProcessor aspects;

    private RecipeProcessor recipes;

    private GeneratorProcessor generatorProcessor;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start ( final BundleContext bundleContext ) throws Exception
    {
        Activator.INSTANCE = this;
        this.aspects = new ChannelAspectProcessor ( bundleContext );
        this.recipes = new RecipeProcessor ( bundleContext );
        this.generatorProcessor = new GeneratorProcessor ( bundleContext );
        this.generatorProcessor.open ();
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( final BundleContext bundleContext ) throws Exception
    {
        this.aspects.close ();
        this.recipes.dispose ();
        this.generatorProcessor.close ();

        Activator.INSTANCE = null;
    }

    public static ChannelAspectProcessor getAspects ()
    {
        return INSTANCE.aspects;
    }

    public static RecipeProcessor getRecipes ()
    {
        return INSTANCE.recipes;
    }

    public static GeneratorProcessor getGeneratorProcessor ()
    {
        return INSTANCE.generatorProcessor;
    }
}
