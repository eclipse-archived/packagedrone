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
package org.eclipse.packagedrone.utils;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Suppressed<T extends Exception> implements AutoCloseable
{
    @FunctionalInterface
    public interface VoidOperation
    {
        public void run () throws Throwable;
    }

    private final Function<Throwable, T> rootCreator;

    private final LinkedList<Throwable> errors = new LinkedList<> ();

    public Suppressed ( final Function<Throwable, T> rootCreator )
    {
        this.rootCreator = rootCreator;
    }

    public Suppressed ( final String message, final BiFunction<String, Throwable, T> rootCreator )
    {
        this.rootCreator = e -> rootCreator.apply ( message, e );
    }

    public void close ( final Closeable closeable )
    {
        try
        {
            closeable.close ();
        }
        catch ( final Exception e )
        {
            this.errors.add ( e );
        }
    }

    public void run ( final VoidOperation op )
    {
        try
        {
            op.run ();
        }
        catch ( final Throwable e )
        {
            this.errors.add ( e );
        }
    }

    private void complete () throws T
    {
        if ( this.errors.isEmpty () )
        {
            return;
        }

        final Throwable e = this.errors.pollFirst ();
        final T root = this.rootCreator.apply ( e );
        for ( final Throwable sup : this.errors )
        {
            root.addSuppressed ( sup );
        }

        this.errors.clear ();

        throw root;
    }

    @Override
    public void close () throws T
    {
        complete ();
    }
}
