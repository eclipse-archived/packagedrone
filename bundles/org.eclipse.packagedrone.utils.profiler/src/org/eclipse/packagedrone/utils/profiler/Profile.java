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
package org.eclipse.packagedrone.utils.profiler;

import java.io.File;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.packagedrone.utils.profiler.internal.ProfilerInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Profile
{
    private final static Logger logger = LoggerFactory.getLogger ( Profile.class );

    private Profile ()
    {
    }

    public static interface Handle extends AutoCloseable
    {
        @Override
        public void close ();

        public Handle createChild ( String operation );

        /**
         * Mark the beginning of a new task
         *
         * @param taskName
         *            the name of the task
         */
        public void task ( String taskName );
    }

    private static Handle NOP = new Handle () {

        @Override
        public void close ()
        {
        }

        @Override
        public Handle createChild ( final String operation )
        {
            return NOP;
        }

        @Override
        public void task ( final String operation )
        {
        }
    };

    public static class DurationEntry
    {
        private final String operation;

        private final Duration duration;

        private List<DurationEntry> entries;

        public DurationEntry ( final String operation, final Duration duration )
        {
            this.operation = operation;
            this.duration = duration;
        }

        public DurationEntry ( final String operation, final Duration duration, final List<DurationEntry> entries )
        {
            this.operation = operation;
            this.duration = duration;
            this.entries = entries;
        }

        public String getOperation ()
        {
            return this.operation;
        }

        public Duration getDuration ()
        {
            return this.duration;
        }

        public List<DurationEntry> getEntries ()
        {
            return this.entries;
        }
    }

    public static class HandleImpl implements Handle
    {
        private final HandleImpl parent;

        private final Instant start;

        private final String operation;

        private final LinkedList<DurationEntry> entries = new LinkedList<> ();

        private Instant taskStart;

        private String taskName;

        public HandleImpl ( final String operation )
        {
            this ( operation, null );
        }

        public HandleImpl ( final String operation, final HandleImpl parent )
        {
            this.parent = parent;
            this.operation = operation;

            activeProfilers.set ( this );

            this.start = Instant.now ();
        }

        @Override
        public Handle createChild ( final String operation )
        {
            return new HandleImpl ( operation, this );
        }

        @Override
        public String toString ()
        {
            return "[" + this.operation + "]";
        }

        @Override
        public void task ( final String taskName )
        {
            debug ( "task : %s", taskName );

            final Instant now = Instant.now ();

            closeTask ( now );

            this.taskStart = now;
            this.taskName = taskName;
        }

        private void closeTask ( final Instant now )
        {
            if ( this.taskStart != null )
            {
                final Duration duration = Duration.between ( this.taskStart, now );
                final DurationEntry entry = new DurationEntry ( this.taskName, duration, Collections.emptyList () );
                this.entries.add ( entry );
            }
        }

        @Override
        public void close ()
        {
            final Instant now = Instant.now ();

            closeTask ( now );

            final Duration duration = Duration.between ( this.start, now );
            final DurationEntry entry = new DurationEntry ( this.operation, duration, this.entries );

            activeProfilers.set ( this.parent );

            if ( this.parent != null )
            {
                debug ( "close : %s : %s", this.parent, this.operation );
                this.parent.entries.add ( entry );
            }
            else
            {
                debug ( "dump : %s", this.operation );

                final ProfileDataHandler handler = getHandler ();
                if ( handler != null )
                {
                    handler.handle ( entry );
                }
            }
        }

    }

    private static ThreadLocal<Handle> activeProfilers = new ThreadLocal<> ();

    private static ProfileDataHandler HANDLER;

    static
    {
        final String value = System.getProperty ( "drone.profile" );
        if ( value != null )
        {
            if ( "true".equalsIgnoreCase ( value ) )
            {
                HANDLER = DumpProfileDataHandler.INSTANCE;
            }
            else if ( value.startsWith ( "xml:" ) )
            {
                final String[] toks = value.split ( ":", 2 );
                if ( toks.length == 2 )
                {
                    try
                    {
                        HANDLER = new XmlProfileDataHandler ( new File ( toks[1] ).toPath () );
                    }
                    catch ( final Throwable e )
                    {
                        logger.error ( "Failed to initialize XML profile data handler", e );
                    }
                }
            }

            if ( HANDLER == null )
            {
                debug ( "Failed to initialize profile data handler. Activated as: '%s'", value );
            }
        }
    }

    private static boolean isActive ()
    {
        return HANDLER != null;
    }

    public static Handle start ( final Object service, final String methodName )
    {
        return start ( makeOperation ( service, methodName ) );
    }

    @SuppressWarnings ( "resource" )
    public static Handle start ( final String operation )
    {
        if ( !isActive () )
        {
            return NOP;
        }
        else
        {
            final Handle handle = activeProfilers.get ();
            if ( handle == null )
            {
                debug ( "create : %s", operation );
                return new HandleImpl ( operation );
            }
            else
            {
                debug ( "child : %s : %s", handle, operation );
                return handle.createChild ( operation );
            }
        }
    }

    private static String makeOperation ( final Object service, final String methodName )
    {
        return String.format ( "%s.%s", service.getClass ().getName (), methodName );
    }

    public static <T> T call ( final Object service, final String methodName, final Callable<T> call )
    {
        return call ( makeOperation ( service, methodName ), call );
    }

    public static <T> T call ( final String operation, final Callable<T> call )
    {
        try ( Handle handle = start ( operation ) )
        {
            return call.call ();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public static void run ( final Object service, final String methodName, final Runnable run )
    {
        run ( makeOperation ( service, methodName ), run );
    }

    public static void run ( final String operation, final Runnable run )
    {
        call ( operation, () -> {
            run.run ();
            return null;
        } );
    }

    public static <T> T proxy ( final T service, final Class<T> iface )
    {
        if ( service == null )
        {
            return null;
        }

        if ( isActive () )
        {
            return iface.cast ( Proxy.newProxyInstance ( service.getClass ().getClassLoader (), new Class<?>[] { iface }, new ProfilerInvocationHandler ( service ) ) );
        }
        else
        {
            return service;
        }
    }

    private static void debug ( final String format, final Object... args )
    {
        if ( Boolean.getBoolean ( "drone.profile.debug" ) )
        {
            System.out.print ( Thread.currentThread ().getName () );
            System.out.print ( ": " );
            System.out.format ( format, args );
            System.out.println ();
        }
    }

    private static ProfileDataHandler getHandler ()
    {
        return HANDLER;
    }
}
