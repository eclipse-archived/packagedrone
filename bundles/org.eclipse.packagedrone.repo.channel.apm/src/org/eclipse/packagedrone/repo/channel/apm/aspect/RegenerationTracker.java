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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.eclipse.packagedrone.utils.Exceptions.ThrowingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegenerationTracker
{
    private final static Logger logger = LoggerFactory.getLogger ( RegenerationTracker.class );

    private final Consumer<Set<String>> func;

    private final ThreadLocal<LinkedList<Set<String>>> states = ThreadLocal.withInitial ( LinkedList::new );

    public RegenerationTracker ( final Consumer<Set<String>> regenerationFunc )
    {
        this.func = regenerationFunc;
    }

    public void run ( final ThrowingRunnable action )
    {
        run ( () -> {
            action.run ();
            return null;
        } );
    }

    public <T> T run ( final Callable<T> action )
    {
        logger.trace ( "Running ..." );

        this.states.get ().push ( new HashSet<> () );

        try
        {
            final T result = action.call ();

            final LinkedList<Set<String>> state = this.states.get ();
            if ( state.size () > 1 )
            {
                // push my items, to my parent
                final Iterator<Set<String>> i = state.iterator ();
                final Set<String> mine = i.next ();
                i.next ().addAll ( mine );
                mine.clear ();
            }
            else
            {
                // flush
                flushAll ();
            }

            return result;
        }
        catch ( final Exception e )
        {
            this.states.get ().peek ().clear (); // clear the current state, the finally will remove it
            throw new RuntimeException ( e );
        }
        finally
        {
            final Set<String> current = this.states.get ().poll (); // always remove from the stack
            if ( !current.isEmpty () )
            {
                throw new IllegalStateException ( "There are still marked artifacts in the finally section" );
            }
        }
    }

    private void flushAll ()
    {
        logger.debug ( "Flush all" );

        Set<String> current;
        while ( ! ( current = this.states.get ().poll () ).isEmpty () )
        {
            logger.trace ( "Flush run: {}", current );

            // replace with new set

            this.states.get ().push ( new HashSet<> () );

            // process set
            this.func.accept ( current );
        }

        this.states.get ().push ( Collections.emptySet () ); // add for balance
    }

    public void mark ( final String artifactId )
    {
        logger.debug ( "Mark '{}' for regeneration", artifactId );

        final LinkedList<Set<String>> state = this.states.get ();
        final Set<String> current = state.peek ();
        if ( current == null )
        {
            throw new IllegalStateException ( "No regeneration context" );
        }
        current.add ( artifactId );
    }

    public boolean isMarked ( final String artifactId )
    {
        final LinkedList<Set<String>> all = this.states.get ();
        for ( final Set<String> level : all )
        {
            if ( level.contains ( artifactId ) )
            {
                return true;
            }
        }
        return false;
    }
}
