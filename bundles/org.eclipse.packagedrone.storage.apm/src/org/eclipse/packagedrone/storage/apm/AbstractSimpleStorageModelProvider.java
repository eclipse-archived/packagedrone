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
package org.eclipse.packagedrone.storage.apm;

public abstract class AbstractSimpleStorageModelProvider<V, W> implements StorageModelProvider<V, W>
{

    private StorageContext context;

    private V viewModel;

    private W writeModel;

    private final Class<W> writeModelClazz;

    public AbstractSimpleStorageModelProvider ( final Class<V> viewClazz, final Class<W> writeClazz )
    {
        this.writeModelClazz = writeClazz;
    }

    @Override
    public V getViewModel ()
    {
        return this.viewModel;
    }

    @Override
    public void start ( final StorageContext context ) throws Exception
    {
        this.context = context;

        this.writeModel = loadWriteModel ( context );
        this.viewModel = makeViewModel ( this.writeModel );
    }

    @Override
    public void stop ()
    {
    }

    @Override
    public W cloneWriteModel ()
    {
        return cloneWriteModel ( this.writeModel );
    }

    @Override
    public void persistWriteModel ( final W writeModel ) throws Exception
    {
        final V viewModel = makeViewModel ( writeModel );

        persistWriteModel ( this.context, writeModel );

        this.writeModel = writeModel;
        this.viewModel = viewModel;
    }

    @Override
    public V makeViewModel ( final Object writeModel )
    {
        return makeViewModelTyped ( this.writeModelClazz.cast ( writeModel ) );
    }

    protected abstract V makeViewModelTyped ( W writeModel );

    protected abstract W cloneWriteModel ( final W writeModel );

    protected abstract void persistWriteModel ( StorageContext context, final W writeModel ) throws Exception;

    protected abstract W loadWriteModel ( StorageContext context ) throws Exception;

}
