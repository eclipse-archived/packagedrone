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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.manage.core.CoreService;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.StorageRegistration;

public class CoreServiceImpl implements CoreService
{

    private static final MetaKey MODEL_KEY = new MetaKey ( "core", "default" );

    private StorageManager manager;

    private StorageRegistration handle;

    public void setStorageManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void start ()
    {
        this.handle = this.manager.registerModel ( 100_000L, MODEL_KEY, new CoreStorageModelProvider () );
    }

    public void stop ()
    {
        this.handle.unregister ();
    }

    @Override
    public String getCoreProperty ( final MetaKey key, final String defaultValue )
    {
        return this.manager.accessCall ( MODEL_KEY, CoreServiceViewModel.class, model -> model.getProperties ().get ( key ) );
    }

    @Override
    public Map<MetaKey, String> getCoreProperties ( final Collection<MetaKey> keys )
    {
        final Map<MetaKey, String> result = new HashMap<> ( keys.size () );

        final Map<MetaKey, String> all = list ();
        for ( final MetaKey key : keys )
        {
            result.put ( key, all.get ( key ) );
        }

        return result;
    }

    @Override
    public void setCoreProperty ( final MetaKey key, final String value )
    {
        this.manager.modifyRun ( MODEL_KEY, CoreServiceModel.class, model -> {
            if ( value == null )
            {
                model.getProperties ().remove ( value );
            }
            else
            {
                model.getProperties ().put ( key, value );
            }
        } );
    }

    @Override
    public Map<MetaKey, String> list ()
    {
        return this.manager.accessCall ( MODEL_KEY, CoreServiceViewModel.class, model -> model.getProperties () );
    }

    @Override
    public void setCoreProperties ( final Map<MetaKey, String> properties )
    {
        this.manager.modifyRun ( MODEL_KEY, CoreServiceModel.class, model -> {
            for ( final Map.Entry<MetaKey, String> entry : properties.entrySet () )
            {
                final String value = entry.getValue ();
                if ( value != null )
                {
                    model.getProperties ().put ( entry.getKey (), value );
                }
                else
                {
                    model.getProperties ().remove ( entry.getKey () );
                }
            }
        } );
    }

}
