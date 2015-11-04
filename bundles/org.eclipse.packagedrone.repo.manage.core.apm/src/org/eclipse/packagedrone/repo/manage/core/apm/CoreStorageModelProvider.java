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
package org.eclipse.packagedrone.repo.manage.core.apm;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.storage.apm.AbstractSimpleStorageModelProvider;
import org.eclipse.packagedrone.storage.apm.StorageContext;
import org.eclipse.packagedrone.storage.apm.util.ReplaceOnCloseWriter;

public class CoreStorageModelProvider extends AbstractSimpleStorageModelProvider<CoreServiceViewModel, CoreServiceModel>
{
    public CoreStorageModelProvider ()
    {
        super ( CoreServiceViewModel.class, CoreServiceModel.class );
    }

    @Override
    public CoreServiceModel cloneWriteModel ( final CoreServiceModel writeModel )
    {
        return new CoreServiceModel ( writeModel );
    }

    @Override
    protected CoreServiceViewModel makeViewModelTyped ( final CoreServiceModel writeModel )
    {
        return new CoreServiceViewModel ( writeModel.getProperties () );
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final CoreServiceModel writeModel ) throws Exception
    {
        try ( ReplaceOnCloseWriter writer = new ReplaceOnCloseWriter ( makePath ( context ), StandardCharsets.UTF_8 ) )
        {
            final Properties p = new Properties ();
            for ( final Map.Entry<MetaKey, String> entry : writeModel.getProperties ().entrySet () )
            {
                final String value = entry.getValue ();
                if ( value == null )
                {
                    continue;
                }

                p.put ( entry.getKey ().toString (), entry.getValue () );
            }
            p.store ( writer, null );

            writer.commit ();
        }
    }

    @Override
    protected CoreServiceModel loadWriteModel ( final StorageContext context ) throws Exception
    {
        final Properties p = new Properties ();
        try ( Reader reader = Files.newBufferedReader ( makePath ( context ) ) )
        {
            p.load ( reader );
        }
        catch ( final NoSuchFileException e )
        {
            // simply ignore
        }

        // now convert to a hash set

        final Map<MetaKey, String> result = new HashMap<> ( p.size () );

        for ( final String key : p.stringPropertyNames () )
        {
            final MetaKey metaKey = MetaKey.fromString ( key );
            result.put ( metaKey, p.getProperty ( key ) );
        }

        return new CoreServiceModel ( result );
    }

    private Path makePath ( final StorageContext context )
    {
        return context.getBasePath ().resolve ( "core.properties" );
    }
}
