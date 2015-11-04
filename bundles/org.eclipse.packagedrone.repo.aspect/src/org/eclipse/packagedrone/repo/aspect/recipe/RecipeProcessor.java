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
package org.eclipse.packagedrone.repo.aspect.recipe;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.aspect.PropertiesHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class RecipeProcessor
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

    private final BundleContext context;

    private final ServiceTracker<Recipe, Entry> tracker;

    private final ServiceTrackerCustomizer<Recipe, Entry> customizer = new ServiceTrackerCustomizer<Recipe, Entry> () {

        @Override
        public void removedService ( final ServiceReference<Recipe> reference, final Entry service )
        {
            RecipeProcessor.this.entries.remove ( service.getInformation ().getId () );
            RecipeProcessor.this.context.ungetService ( reference );
        }

        @Override
        public void modifiedService ( final ServiceReference<Recipe> reference, final Entry service )
        {
        }

        @Override
        public Entry addingService ( final ServiceReference<Recipe> reference )
        {
            final Entry entry = makeEntry ( reference );

            if ( entry != null )
            {
                RecipeProcessor.this.entries.put ( entry.getInformation ().getId (), entry );
            }

            return entry;
        }
    };

    private final Map<String, Entry> entries = new ConcurrentHashMap<> ();

    public RecipeProcessor ( final BundleContext context )
    {
        this.context = context;

        this.tracker = new ServiceTracker<Recipe, Entry> ( context, Recipe.class, this.customizer );
        this.tracker.open ();
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    public Collection<RecipeInformation> getRecipes ()
    {
        return this.entries.values ().stream ().map ( Entry::getInformation ).collect ( Collectors.toSet () );
    }

    public <C extends Comparable<C>> List<RecipeInformation> getSortedRecipes ( final Function<RecipeInformation, C> func )
    {
        return this.entries.values ().stream ().map ( Entry::getInformation ).sorted ( new GenericComparator<RecipeInformation, C> ( func ) ).collect ( Collectors.toList () );
    }

    public void process ( final String id, final Consumer<Recipe> recipe ) throws RecipeNotFoundException
    {
        final Entry entry = this.entries.get ( id );

        if ( entry == null )
        {
            throw new RecipeNotFoundException ( id );
        }

        recipe.accept ( entry.getRecipe () );
    }

    protected Entry makeEntry ( final ServiceReference<Recipe> reference )
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

        // get service

        final Recipe recipe = this.context.getService ( reference );

        // create result

        return new Entry ( new RecipeInformation ( id, label, description ), recipe );
    }

    protected static String getString ( final ServiceReference<?> ref, final String name )
    {
        final Object val = ref.getProperty ( name );
        if ( val instanceof String )
        {
            return (String)val;
        }
        return null;
    }
}
