/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Consumer;

import org.eclipse.packagedrone.web.LinkTarget;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class GeneratorProcessor
{
    private final ServiceTracker<ArtifactGenerator, ArtifactGenerator> tracker;

    public GeneratorProcessor ( final BundleContext context )
    {
        this.tracker = new ServiceTracker<ArtifactGenerator, ArtifactGenerator> ( context, ArtifactGenerator.class, null );
    }

    public void open ()
    {
        this.tracker.open ();
    }

    public void close ()
    {
        this.tracker.close ();
    }

    protected Map<String, ArtifactGenerator> getAllFactories ()
    {
        final SortedMap<ServiceReference<ArtifactGenerator>, ArtifactGenerator> tracked = this.tracker.getTracked ();

        final Map<String, ArtifactGenerator> result = new HashMap<> ( tracked.size () );

        for ( final Map.Entry<ServiceReference<ArtifactGenerator>, ArtifactGenerator> entry : tracked.entrySet () )
        {
            final Object key = entry.getKey ().getProperty ( ArtifactGenerator.GENERATOR_ID_PROPERTY );
            if ( ! ( key instanceof String ) )
            {
                continue;
            }
            result.put ( (String)key, entry.getValue () );
        }

        return result;
    }

    public void process ( final String generatorId, final Consumer<ArtifactGenerator> consumer )
    {
        final ArtifactGenerator gen = getAllFactories ().get ( generatorId );
        if ( gen == null )
        {
            throw new IllegalStateException ( String.format ( "Artifact generator '%s' is not registered", generatorId ) );
        }
        consumer.accept ( gen );
    }

    public void process ( final String generatorId, final GenerationContext context ) throws Exception
    {
        process ( generatorId, ( gen ) -> {
            try
            {
                gen.generate ( context );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        } );
    }

    public Map<String, GeneratorInformation> getInformations ()
    {
        final Map<String, GeneratorInformation> result = new HashMap<> ();

        for ( final Map.Entry<ServiceReference<ArtifactGenerator>, ArtifactGenerator> gen : this.tracker.getTracked ().entrySet () )
        {
            final String id = getString ( gen.getKey (), ArtifactGenerator.GENERATOR_ID_PROPERTY );
            final String label = getString ( gen.getKey (), Constants.SERVICE_PID );
            final String description = getString ( gen.getKey (), Constants.SERVICE_DESCRIPTION );

            if ( id != null )
            {
                final LinkTarget addTarget = gen.getValue ().getAddTarget ();
                result.put ( id, new GeneratorInformation ( id, label, description, addTarget ) );
            }
        }

        return result;
    }

    private static String getString ( final ServiceReference<?> ref, final String name )
    {
        final Object v = ref.getProperty ( name );
        if ( v == null )
        {
            return null;
        }
        return v.toString ();
    }

}
