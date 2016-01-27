/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.packagedrone.repo.aspect.PropertiesHelper;
import org.eclipse.packagedrone.utils.osgi.FactoryTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class RecipeProcessor extends FactoryTracker<Recipe, RecipeProcessor.Entry>
{
    public static class GenericComparator<T, C extends Comparable<C>> implements Comparator<T>
    {
        private final Function<T, C> func;

        public GenericComparator ( final Function<T, C> func )
        {
            this.func = func;
        }

        @Override
        public int compare ( final T o1, final T o2 )
        {
            final C v1 = this.func.apply ( o1 );
            final C v2 = this.func.apply ( o2 );

            if ( v1 == v2 )
            {
                return 0;
            }

            if ( v1 == null )
            {
                return -1;
            }

            return v1.compareTo ( v2 );
        }
    }

    public static class Entry
    {
        private final RecipeInformation information;

        private final Recipe recipe;

        public Entry ( final RecipeInformation information, final Recipe recipe )
        {
            this.information = information;
            this.recipe = recipe;
        }

        public RecipeInformation getInformation ()
        {
            return this.information;
        }

        public Recipe getRecipe ()
        {
            return this.recipe;
        }
    }

    public RecipeProcessor ( final BundleContext context )
    {
        super ( context, Recipe.class );
        open ();
    }

    public void dispose ()
    {
        close ();
    }

    public List<RecipeInformation> getRecipes ()
    {
        final List<RecipeInformation> result = new ArrayList<> ();
        consumeAll ( stream -> stream.forEach ( entry -> result.add ( entry.getInformation () ) ) );
        return result;
    }

    public <C extends Comparable<C>> List<RecipeInformation> getSortedRecipes ( final Function<RecipeInformation, C> func )
    {
        final List<RecipeInformation> result = getRecipes ();
        Collections.sort ( result, new GenericComparator<RecipeInformation, C> ( func ) );
        return result;
    }

    public void process ( final String id, final Consumer<Recipe> recipe ) throws RecipeNotFoundException
    {
        consume ( id, entry -> recipe.accept ( entry.getRecipe () ), () -> new RecipeNotFoundException ( id ) );
    }

    @Override
    protected String getFactoryId ( final ServiceReference<Recipe> reference )
    {
        return getString ( reference, Constants.SERVICE_PID );
    }

    @Override
    protected Entry mapService ( final ServiceReference<Recipe> reference, final Recipe service )
    {
        // get id

        final String id = getString ( reference, Constants.SERVICE_PID );
        if ( id == null )
        {
            return null;
        }

        // get label

        String label = getString ( reference, "drone.label" );
        if ( label == null )
        {
            label = getString ( reference, Constants.SERVICE_DESCRIPTION );
        }

        // get or load description

        String description = getString ( reference, "drone.description" );
        if ( description == null )
        {
            description = PropertiesHelper.loadUrl ( reference.getBundle (), getString ( reference, "drone.description.url" ) );
        }

        return new Entry ( new RecipeInformation ( id, label, description ), service );
    }
}
