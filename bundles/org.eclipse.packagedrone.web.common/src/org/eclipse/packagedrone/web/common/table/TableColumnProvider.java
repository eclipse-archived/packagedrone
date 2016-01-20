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
package org.eclipse.packagedrone.web.common.table;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;

public interface TableColumnProvider
{
    public @NonNull TableColumn getColumn ();

    public void provideContent ( @NonNull TableDescriptor descriptor, Object object, @NonNull PrintWriter out ) throws IOException;

    public static <T> TableColumnProvider stringProvider ( @NonNull final TableColumn column, @NonNull final Class<T> clazz, @NonNull final Function<T, String> func )
    {
        return provider ( column, clazz, ( item, out ) -> {
            final String value = func.apply ( item );
            if ( value != null )
            {
                out.write ( value );
            }
        } );
    }

    public static <T> TableColumnProvider provider ( @NonNull final TableColumn column, @NonNull final Class<T> clazz, @NonNull final ContentProvider<T> provider )
    {
        return new TableColumnProvider () {

            @Override
            public @NonNull TableColumn getColumn ()
            {
                return column;
            }

            @Override
            public void provideContent ( @NonNull final TableDescriptor descriptor, final Object object, @NonNull final PrintWriter out ) throws IOException
            {
                if ( object == null )
                {
                    return;
                }

                if ( clazz.isAssignableFrom ( object.getClass () ) )
                {
                    @SuppressWarnings ( "null" )
                    @NonNull
                    final T value = (@NonNull T)clazz.cast ( object );
                    provider.provide ( value, out );
                }
            }
        };
    }
}
