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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;

public interface TableExtension
{
    public @NonNull TableDescriptor geTableDescriptor ();

    public default List<TableColumn> getColumns ()
    {
        return getColumns ( Integer.MIN_VALUE, Integer.MAX_VALUE );
    }

    public default void extend ( @NonNull final Object value, @NonNull final PrintWriter out ) throws IOException
    {
        extend ( value, out, Integer.MIN_VALUE, Integer.MAX_VALUE );
    }

    @SuppressWarnings ( "null" )
    public default @NonNull List<TableColumn> getColumns ( final int fromPriority, final int toPriority )
    {
        return streamProviders ( fromPriority, toPriority ).map ( TableColumnProvider::getColumn ).collect ( Collectors.toList () );
    }

    public default void extend ( @NonNull final Object value, @NonNull final PrintWriter out, final int fromPriority, final int toPriority ) throws IOException
    {
        for ( final TableColumnProvider provider : (Iterable<TableColumnProvider>)streamProviders ( fromPriority, toPriority )::iterator )
        {
            if ( provider.getColumn ().getPriority () < fromPriority )
            {
                continue;
            }
            if ( provider.getColumn ().getPriority () > toPriority )
            {
                break;
            }
            provider.provideContent ( geTableDescriptor (), value, out );
        }
    }

    public default List<TableColumnProvider> getProviders ()
    {
        return getProviders ( Integer.MIN_VALUE, Integer.MAX_VALUE );
    }

    public default List<TableColumnProvider> getProviders ( final int fromPriority, final int toPriority )
    {
        return streamProviders ( fromPriority, toPriority ).collect ( Collectors.toList () );
    }

    public Stream<TableColumnProvider> streamProviders ( int fromPriority, int toPriority );
}
