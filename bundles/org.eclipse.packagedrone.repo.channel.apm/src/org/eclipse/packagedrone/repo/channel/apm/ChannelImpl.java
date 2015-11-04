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
package org.eclipse.packagedrone.repo.channel.apm;

import static org.eclipse.packagedrone.utils.Exceptions.wrapException;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.IdTransformer;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.provider.AccessContext;
import org.eclipse.packagedrone.repo.channel.provider.Channel;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.StorageRegistration;
import org.osgi.service.event.EventAdmin;

public class ChannelImpl implements Channel
{
    private final String id;

    private final MetaKey storageKey;

    private final StorageManager manager;

    private final ChannelProviderImpl provider;

    private final StorageRegistration handle;

    public ChannelImpl ( final String id, final EventAdmin eventAdmin, final MetaKey storageKey, final StorageManager manager, final ChannelProviderImpl provider )
    {
        this.id = id;

        this.storageKey = storageKey;
        this.manager = manager;

        this.provider = provider;

        this.handle = manager.registerModel ( 10_000, storageKey, new ChannelModelProvider ( eventAdmin, id ) );
    }

    public void dispose ()
    {
        this.handle.unregister ();
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public <T> T accessCall ( final ChannelOperation<T, AccessContext> operation, final IdTransformer idTransformer )
    {
        return this.manager.accessCall ( this.storageKey, AccessContext.class, model -> wrapException ( () -> {
            ( (ModifyContextImpl)model ).setIdTransformer ( idTransformer );
            return operation.process ( model );
        } ) );
    }

    @Override
    public <T> T modifyCall ( final ChannelOperation<T, ModifyContext> operation, final IdTransformer idTransformer )
    {
        return this.manager.modifyCall ( this.storageKey, ModifyContext.class, model -> wrapException ( () -> {
            ( (ModifyContextImpl)model ).setIdTransformer ( idTransformer );
            return operation.process ( model );
        } ) );
    }

    @Override
    public void delete ()
    {
        this.handle.unregister ();
        this.provider.deleteChannel ( this );
    }
}
