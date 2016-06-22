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
package org.eclipse.packagedrone.storage.apm;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An even simpler abstract base for storing JSON based models
 * <p>
 * This abstract base makes the assumption that the view model is actually
 * backed by the same implementation as the write model.
 * </p>
 *
 * @param <V>
 *            the view model
 * @param <W>
 *            the write model
 * @param <G>
 *            the GSON persistence model
 */
public abstract class AbstractSimplerGsonStorageModelProvider<V, W, G> extends AbstractSimpleGsonStorageModelProvider<V, W, G>
{
    private final String fileName;

    private final Supplier<W> newWriteModel;

    private final Function<W, W> cloneWriteModel;

    private final Function<W, V> makeViewModel;

    public AbstractSimplerGsonStorageModelProvider ( final Class<W> writeClazz, final Class<G> gsonModelClass, final String fileName, final Supplier<W> newWriteModel, final Function<W, W> cloneWriteModel, final Function<W, V> makeViewModel )
    {
        super ( writeClazz, gsonModelClass );
        this.fileName = Objects.requireNonNull ( fileName );
        this.newWriteModel = Objects.requireNonNull ( newWriteModel );
        this.cloneWriteModel = Objects.requireNonNull ( cloneWriteModel );
        this.makeViewModel = Objects.requireNonNull ( makeViewModel );
    }

    public AbstractSimplerGsonStorageModelProvider ( final Class<V> viewClazz, final Class<W> writeClazz, final Class<G> gsonModelClass, final String fileName, final Supplier<W> newWriteModel, final Function<W, W> cloneWriteModel )
    {
        super ( writeClazz, gsonModelClass );
        this.fileName = Objects.requireNonNull ( fileName );
        this.newWriteModel = Objects.requireNonNull ( newWriteModel );
        this.cloneWriteModel = Objects.requireNonNull ( cloneWriteModel );
        this.makeViewModel = writeModel -> defaultMakeViewModelTyped ( writeModel, viewClazz );
    }

    private V defaultMakeViewModelTyped ( final W writeModel, final Class<V> viewClazz )
    {
        final W viewModel = cloneWriteModel ( writeModel );

        final Object proxyInstance = Proxy.newProxyInstance ( writeModel.getClass ().getClassLoader (), new Class<?>[] { viewClazz }, ( proxy, method, args ) -> method.invoke ( viewModel, args ) );

        return viewClazz.cast ( proxyInstance );
    }

    @Override
    protected W createNewModel ()
    {
        return this.newWriteModel.get ();
    }

    @Override
    protected Path makePath ( final StorageContext context )
    {
        return context.getBasePath ().resolve ( this.fileName );
    }

    @Override
    protected W cloneWriteModel ( final W writeModel )
    {
        if ( writeModel == null )
        {
            return createNewModel ();
        }
        return this.cloneWriteModel.apply ( writeModel );
    }

    @Override
    protected V makeViewModelTyped ( final W writeModel )
    {
        return this.makeViewModel.apply ( writeModel );
    }
}
