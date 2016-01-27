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

public interface TableColumnProvider
{
    public TableColumn getColumn ();

    public void provideContent ( TableDescriptor descriptor, Object object, PrintWriter out ) throws IOException;

    public static <T> TableColumnProvider stringProvider ( final TableColumn column, final Class<T> clazz, final Function<T, String> func )
    {
        return provider ( column, clazz, ( item, out ) -> {
            final String value = func.apply ( item );
            if ( value != null )
            {
                out.write ( value );
            }
        } );
    }

    public static <T> TableColumnProvider provider ( final TableColumn column, final Class<T> clazz, final ContentProvider<T> provider )
    {
        return new TableColumnProvider () {

            @Override
            public TableColumn getColumn ()
            {
                return column;
            }

            @Override
            public void provideContent ( final TableDescriptor descriptor, final Object object, final PrintWriter out ) throws IOException
            {
                if ( object == null )
                {
                    return;
                }

                if ( clazz.isAssignableFrom ( object.getClass () ) )
                {
                    final T value = clazz.cast ( object );
                    provider.provide ( value, out );
                }
            }
        };
    }
}
