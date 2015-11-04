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
package org.eclipse.packagedrone.job;

import java.util.function.Supplier;

import org.eclipse.packagedrone.job.JobInstance.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class AbstractJsonJobFactory<T, R> implements JobFactory
{
    private static GsonBuilder DEFAULT = new GsonBuilder ();

    private Supplier<GsonBuilder> gsonBuilder;

    private Class<T> dataClazz;

    private GsonBuilder builder;

    public AbstractJsonJobFactory ( final Class<T> dataClazz, final Supplier<GsonBuilder> gsonBuilder )
    {
        this.dataClazz = dataClazz;
        this.gsonBuilder = gsonBuilder == null ? ( ) -> DEFAULT : gsonBuilder;
    }

    public AbstractJsonJobFactory ( final Class<T> dataClazz, final GsonBuilder gsonBuilder )
    {
        this.dataClazz = dataClazz;
        this.gsonBuilder = null;
        this.builder = gsonBuilder;
    }

    public AbstractJsonJobFactory ( final Class<T> dataClazz )
    {
        this ( dataClazz, new GsonBuilder () );
    }

    protected Gson createGson ()
    {
        if ( this.builder == null )
        {
            this.builder = this.gsonBuilder.get ();
        }
        return this.builder.create ();
    }

    protected abstract R process ( Context context, T data ) throws Exception;

    @Override
    public JobInstance createInstance ( final String data )
    {
        final Gson gson = createGson ();

        final T cfg = gson.fromJson ( data, this.dataClazz );

        return new JobInstance () {

            @Override
            public void run ( final Context context ) throws Exception
            {
                final R result = process ( context, cfg );
                context.setResult ( gson.toJson ( result ) );
            }
        };
    }

    @Override
    public String encodeConfiguration ( final Object data )
    {
        if ( data == null )
        {
            return null;
        }

        if ( !this.dataClazz.isAssignableFrom ( data.getClass () ) )
        {
            throw new IllegalArgumentException ( String.format ( "Job data must be of type: %s (is %s)", this.dataClazz, data.getClass () ) );
        }

        return createGson ().toJson ( data );
    }

    @Override
    public String makeLabel ( final String data )
    {
        final T cfg = createGson ().fromJson ( data, this.dataClazz );
        return makeLabelFromData ( cfg );
    }

    protected abstract String makeLabelFromData ( T data );
}
