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

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.packagedrone.repo.MetaKey;

public class StorageManager
{
    private static final ThreadLocal<Deque<State>> lockStates = ThreadLocal.withInitial ( LinkedList::new );

    private long counter;

    private final ReadWriteLock modelLock = new ReentrantReadWriteLock ( false );

    private final Map<Long, Entry> modelIdMap = new HashMap<> ();

    private final Map<MetaKey, Entry> modelKeyMap = new HashMap<> ();

    private final StorageContext context;

    private boolean closed;

    private static enum LockType
    {
        READ,
        WRITE;
    }

    private static class State
    {
        /**
         * The parent state, may be <code>null</code>
         */
        private final State parent;

        /**
         * This will hold the first parent (nearest to the root) with the same
         * key, may be <code>null</code>
         */
        private final State sameKeyParent;

        private final long priority;

        private final MetaKey key;

        private final State highestState;

        private final LockType type;

        private Object writeModel;

        private List<Runnable> afterTasks;

        public State ( final State parent, final LockType type, final long priority, final MetaKey key )
        {
            this.parent = parent;
            this.priority = priority;
            this.key = key;
            this.type = type;

            final State sameParent = getSameParent ( parent, key );
            if ( sameParent != null && sameParent.sameKeyParent != null )
            {
                this.sameKeyParent = sameParent.sameKeyParent;
            }
            else
            {
                this.sameKeyParent = sameParent;
            }

            if ( parent == null )
            {
                this.highestState = this;
            }
            else
            {
                this.highestState = compareTwo ( priority, key, parent.priority, parent.key ) > 0 ? this : parent;
            }
        }

        public void setWriteModel ( final Object writeModel )
        {
            this.writeModel = writeModel;
        }

        public Object getWriteModel ()
        {
            return this.writeModel;
        }

        public static int compareTo ( final State state1, final State state2 )
        {
            return compareTwo ( state1.priority, state1.key, state2.priority, state2.key );
        }

        private static int compareTwo ( final long priority1, final MetaKey key1, final long priority2, final MetaKey key2 )
        {
            final int rc = Long.compare ( priority1, priority2 );
            if ( rc != 0 )
            {
                return rc;
            }

            return key1.compareTo ( key2 );
        }

        public State getHighestState ()
        {
            return this.highestState;
        }

        @Override
        public String toString ()
        {
            return String.format ( "%s -> %s|%s", this.parent != null ? this.parent : "ROOT", this.priority, this.key );
        }

        public boolean isLocked ( final MetaKey key )
        {
            return getSameParent ( this, key ) != null;
        }

        public boolean isReadLocked ( final MetaKey key )
        {
            State current = this;

            while ( current != null )
            {
                if ( current.key.equals ( key ) && current.type == LockType.READ )
                {
                    return true;
                }

                current = current.parent;
            }
            return false;
        }

        public void addAfterTask ( final Runnable runnable )
        {
            if ( this.afterTasks == null )
            {
                this.afterTasks = new LinkedList<> ();
            }

            this.afterTasks.add ( runnable );
        }

        public void runAfterTasks ()
        {
            if ( this.afterTasks == null )
            {
                return;
            }

            LinkedList<Exception> errors = null;
            for ( final Runnable runnable : this.afterTasks )
            {
                try
                {
                    runnable.run ();
                }
                catch ( final Exception e )
                {
                    if ( errors == null )
                    {
                        errors = new LinkedList<> ();
                    }
                    errors.add ( e );
                }
            }

            this.afterTasks.clear ();

            handleErrors ( "Failed to run 'after' tasks", errors );
        }
    }

    /**
     * Traverse up from the provided parent and find the first which uses the
     * same key
     *
     * @param parent
     *            the first parent candidate
     * @param key
     *            the key to look for
     * @return the first parent (which may be the input parameter), which uses
     *         the same key
     */
    private static State getSameParent ( final State parent, final MetaKey key )
    {
        State current = parent;
        while ( current != null )
        {
            if ( current.key.equals ( key ) )
            {
                return current;
            }

            current = current.parent;
        }
        return null;
    }

    private static class Entry
    {
        @SuppressWarnings ( "unused" )
        long id;

        long lockPriority;

        MetaKey key;

        StorageModelProvider<?, ?> storageProvider;

        ReadWriteLock lock = new ReentrantReadWriteLock ( false );

        public Entry ( final long id, final long lockPriority, final MetaKey key, final StorageModelProvider<?, ?> storageProvider )
        {
            this.id = id;
            this.lockPriority = lockPriority;
            this.key = key;
            this.storageProvider = storageProvider;
        }
    }

    public StorageManager ( final Path basePath )
    {
        validateBasePath ( basePath );

        this.context = new StorageContext () {

            @Override
            public Path getBasePath ()
            {
                return basePath;
            }
        };
    }

    private void validateBasePath ( final Path basePath )
    {
        if ( Files.exists ( basePath ) )
        {
            if ( !Files.isDirectory ( basePath ) )
            {
                throw new IllegalStateException ( String.format ( "Base path '%s' already exists but is not a directory" ) );
            }
            if ( !Files.isWritable ( basePath ) )
            {
                throw new IllegalStateException ( String.format ( "Base path '%s' already exists but is not writable" ) );
            }
        }
        else
        {
            try
            {
                Files.createDirectories ( basePath );
            }
            catch ( final FileAlreadyExistsException e )
            {
                // silently ignore
            }
            catch ( final IOException e )
            {
                throw new IllegalStateException ( "Failed to create base path", e );
            }
        }
    }

    /**
     * Register a new model with the storage manager
     *
     * @param lockPriority
     *            the priority of the lock, lower numbers must be locked before
     *            higher numbers
     * @param key
     *            the model key
     * @param storageProvider
     *            the storage provider
     * @return a handle for unregistering the model
     * @throws ModelInitializationException
     *             if the {@link StorageModelProvider#start(StorageContext)}
     *             method fails
     */
    public StorageRegistration registerModel ( final long lockPriority, final MetaKey key, final StorageModelProvider<?, ?> storageProvider ) throws ModelInitializationException
    {
        this.modelLock.writeLock ().lock ();
        try
        {
            testClosed ();

            if ( this.modelKeyMap.containsKey ( key ) )
            {
                throw new IllegalArgumentException ( String.format ( "A provider for '%s' is already registered", key ) );
            }

            try
            {
                storageProvider.start ( this.context );
            }
            catch ( final Exception e )
            {
                throw new ModelInitializationException ( "Failed to start model provider: " + key, e );
            }

            final long id = this.counter++;

            final Entry entry = new Entry ( id, lockPriority, key, storageProvider );

            this.modelIdMap.put ( id, entry );
            this.modelKeyMap.put ( key, entry );

            return new StorageRegistration () {

                @Override
                public void unregister ()
                {
                    unregisterModel ( id );
                }
            };
        }
        finally
        {
            this.modelLock.writeLock ().unlock ();
        }
    }

    protected void unregisterModel ( final long id )
    {
        final Entry entry;

        this.modelLock.writeLock ().lock ();
        try
        {
            if ( this.closed )
            {
                // ignore when closing
                return;
            }

            entry = this.modelIdMap.remove ( id );
            if ( entry != null )
            {
                this.modelKeyMap.remove ( entry.key );
            }
        }
        finally
        {
            this.modelLock.writeLock ().unlock ();
        }

        if ( entry != null )
        {
            entry.storageProvider.stop ();
        }
    }

    public <T, M> T accessCall ( final MetaKey modelKey, final Class<M> modelClazz, final Function<M, T> function )
    {
        return doWithModel ( modelKey, entry -> {

            return doWithState ( entry, LockType.READ, ( state ) -> {

                entry.lock.readLock ().lock ();
                try
                {
                    final State same = state.sameKeyParent;

                    final Object viewModel;
                    if ( same != null && same.writeModel != null )
                    {
                        viewModel = entry.storageProvider.makeViewModel ( same.writeModel );
                    }
                    else
                    {
                        viewModel = entry.storageProvider.getViewModel ();
                    }

                    if ( viewModel == null || modelClazz.isAssignableFrom ( viewModel.getClass () ) )
                    {
                        return function.apply ( modelClazz.cast ( viewModel ) );
                    }
                    else
                    {
                        throw new IllegalStateException ( String.format ( "View model of '%s' is not of type '%s'", modelKey, modelClazz.getName () ) );
                    }
                }
                finally
                {
                    entry.lock.readLock ().unlock ();
                }

            } );
        } );

    }

    public <T, M> T modifyCall ( final MetaKey modelKey, final Class<M> modelClazz, final Function<M, T> function )
    {
        return doWithModel ( modelKey, entry -> {

            return doWithState ( entry, LockType.WRITE, ( state ) -> {

                entry.lock.writeLock ().lock ();
                try
                {
                    return performOperation ( entry, modelClazz, function );
                }
                finally
                {
                    entry.lock.writeLock ().unlock ();
                }
            } );
        } );
    }

    private <T, M> T performOperation ( final Entry entry, final Class<M> modelClazz, final Function<M, T> function )
    {
        @SuppressWarnings ( "unchecked" )
        final StorageModelProvider<?, M> sp = (StorageModelProvider<?, M>)entry.storageProvider;

        final State current = getCurrent ();

        // try to get same from parent of current
        final State same = current.sameKeyParent;

        final M writeModel;

        if ( same == null )
        {
            writeModel = cast ( entry, modelClazz, sp.cloneWriteModel () );
        }
        else
        {
            writeModel = cast ( entry, modelClazz, same.getWriteModel () );
        }
        current.setWriteModel ( writeModel );

        // call user code
        final T result = function.apply ( writeModel );

        if ( same == null )
        {
            // we are the one that cloned the model
            try
            {
                // we received the same model type from cloneWriteModel ()
                sp.persistWriteModel ( writeModel );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( String.format ( "Failed to persist model of %s", entry.key ), e );
            }

            current.runAfterTasks ();
        }
        // otherwise: -> save later

        return result;
    }

    private static <T> T cast ( final Entry entry, final Class<T> clazz, final Object o )
    {
        if ( o == null || clazz.isAssignableFrom ( o.getClass () ) )
        {
            return clazz.cast ( o );
        }

        throw new IllegalStateException ( String.format ( "Model of '%s' is not of type '%s'", entry.key, clazz.getName () ) );
    }

    protected <T> T doWithModel ( final MetaKey modelKey, final Function<Entry, T> function )
    {
        this.modelLock.readLock ().lock ();

        try
        {
            testClosed ();

            final Entry entry = this.modelKeyMap.get ( modelKey );

            if ( entry == null )
            {
                throw new IllegalArgumentException ( String.format ( "Model '%s' could not be found", modelKey ) );
            }

            return function.apply ( entry );
        }
        finally
        {
            this.modelLock.readLock ().unlock ();
        }
    }

    protected static <T> T doWithState ( final Entry entry, final LockType type, final Function<State, T> function )
    {
        final Deque<State> stack = lockStates.get ();

        final State current = stack.peekLast ();
        final State next = new State ( current, type, entry.lockPriority, entry.key );

        if ( current != null )
        {
            if ( type == LockType.WRITE && current.isReadLocked ( entry.key ) )
            {
                // lock upgrade is not allowed -> fail
                throw new IllegalStateException ( String.format ( "%s is already read locked. Upgrading read locks to write locks is not supported! (State: %s)", entry.key, current ) );
            }

            if ( !current.isLocked ( entry.key ) )
            {
                if ( State.compareTo ( current.getHighestState (), next ) >= 0 )
                {
                    // last is higher -> fail
                    throw new IllegalStateException ( String.format ( "Lock is lower or equal in priority than previous lock: %s -> %s", current, next ) );
                }
            }
        }

        stack.addLast ( next );

        try
        {
            return function.apply ( next );
        }
        finally
        {
            stack.removeLast ();
        }
    }

    protected static State getCurrent ()
    {
        final Deque<State> stack = lockStates.get ();
        return stack.peekLast ();
    }

    public StorageContext getContext ()
    {
        testClosed ();

        return this.context;
    }

    public <M> void accessRun ( final MetaKey modelKey, final Class<M> modelClazz, final Consumer<M> consumer )
    {
        accessCall ( modelKey, modelClazz, ( model ) -> {
            consumer.accept ( model );
            return null;
        } );
    }

    public <M> void modifyRun ( final MetaKey modelKey, final Class<M> modelClazz, final Consumer<M> consumer )
    {
        modifyCall ( modelKey, modelClazz, ( model ) -> {
            consumer.accept ( model );
            return null;
        } );
    }

    /**
     * Execute a task after the current state has been persisted
     * <p>
     * If the current state will not persist the model (because a parent opened
     * the model before and is required to perform persisting the model), then
     * the task will be executed at the time the parent state causes persisting
     * the model.
     * </p>
     * <p>
     * If there is no open state or the parent state is read-lock, the runnable
     * will be executed immediately.
     * </p>
     *
     * @param runnable
     *            the runnable to execute
     */
    public static void executeAfterPersist ( final Runnable runnable )
    {
        final State state = getCurrent ();
        if ( state == null )
        {
            runnable.run ();
            return;
        }

        final MetaKey key = state.key;
        if ( key == null )
        {
            runnable.run ();
            return;
        }

        final State parent = state.sameKeyParent == null ? state : state.sameKeyParent;

        if ( parent.type == LockType.READ )
        {
            runnable.run ();
            return;
        }

        parent.addAfterTask ( runnable );
    }

    public void close ()
    {
        List<Entry> providers;
        this.modelLock.writeLock ().lock ();
        try
        {
            if ( this.closed )
            {
                return;
            }

            providers = new ArrayList<> ( this.modelIdMap.values () );

            this.closed = true;

            this.modelIdMap.clear ();
            this.modelKeyMap.clear ();
        }
        finally
        {
            this.modelLock.writeLock ().unlock ();
        }

        // we closed it, so close all storages

        final LinkedList<Exception> allErrors = new LinkedList<> ();

        for ( final Entry entry : providers )
        {
            try
            {
                // nobody else can hold a lock here, we had been in write lock before
                // and cleared the maps
                entry.storageProvider.stop ();
            }
            catch ( final Exception e )
            {
                allErrors.add ( e );
            }
        }

        // handle errors

        handleErrors ( "Failed to close provider", allErrors );
    }

    private static void handleErrors ( final String message, final LinkedList<Exception> allErrors )
    {
        if ( allErrors == null )
        {
            return;
        }

        if ( !allErrors.isEmpty () )
        {
            final RuntimeException e = new RuntimeException ( message, allErrors.poll () );

            // add remaining
            allErrors.forEach ( e::addSuppressed );

            throw e;
        }
    }

    protected void testClosed ()
    {
        if ( this.closed )
        {
            throw new IllegalStateException ( "Storage is already closed" );
        }
    }

}
