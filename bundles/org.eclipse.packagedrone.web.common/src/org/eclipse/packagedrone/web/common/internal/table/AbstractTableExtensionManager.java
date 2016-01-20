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
package org.eclipse.packagedrone.web.common.internal.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.packagedrone.web.common.table.TableColumnProvider;
import org.eclipse.packagedrone.web.common.table.TableDescriptor;
import org.eclipse.packagedrone.web.common.table.TableExtender;
import org.eclipse.packagedrone.web.common.table.TableExtension;
import org.eclipse.packagedrone.web.common.table.TableExtensionManager;

public abstract class AbstractTableExtensionManager implements TableExtensionManager
{
    private final class TableExtensionImpl implements TableExtension
    {
        private @NonNull final TableDescriptor descriptor;

        private @NonNull final List<TableColumnProvider> providers;

        @SuppressWarnings ( "null" )
        private TableExtensionImpl ( @NonNull final TableDescriptor descriptor, @NonNull final List<TableColumnProvider> providers )
        {
            Collections.sort ( providers, Comparator.comparing ( provider -> provider.getColumn ().getPriority () ) );
            this.descriptor = descriptor;
            this.providers = Collections.unmodifiableList ( providers );
        }

        @Override
        public @NonNull TableDescriptor geTableDescriptor ()
        {
            return this.descriptor;
        }

        @Override
        public Stream<TableColumnProvider> streamProviders ( final int fromPriority, final int toPriority )
        {
            Stream<TableColumnProvider> s = this.providers.stream ();
            if ( fromPriority != Integer.MIN_VALUE || toPriority != Integer.MAX_VALUE )
            {
                s = s.filter ( p -> {
                    final int prio = p.getColumn ().getPriority ();
                    return prio >= fromPriority && prio < toPriority;
                } );
            }

            return s;
        }
    }

    protected abstract void access ( final Consumer<Collection<TableExtender>> extenders );

    @Override
    public @NonNull TableExtension createExtensions ( @NonNull final HttpServletRequest request, @NonNull final TableDescriptor descriptor )
    {
        final List<TableColumnProvider> providers = new ArrayList<> ();

        access ( extenders -> {
            for ( final TableExtender extender : extenders )
            {
                extender.getColumns ( request, descriptor, providers::add );
            }
        } );

        return new TableExtensionImpl ( descriptor, providers );
    }
}
