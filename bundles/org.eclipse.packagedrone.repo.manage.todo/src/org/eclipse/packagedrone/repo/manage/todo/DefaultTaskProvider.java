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
package org.eclipse.packagedrone.repo.manage.todo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTaskProvider implements TaskProvider
{
    private final static Logger logger = LoggerFactory.getLogger ( DefaultTaskProvider.class );

    private final Set<TaskListener> listeners = new HashSet<> ();

    private Set<Task> tasks = new CopyOnWriteArraySet<> ();

    public DefaultTaskProvider ()
    {
    }

    protected void fireNotify ()
    {
        final Collection<? extends Task> current = getTasks ();

        for ( final TaskListener listener : this.listeners )
        {
            try
            {
                listener.tasksChanged ( current );
            }
            catch ( final Exception e )
            {
                logger.warn ( "Failed to notify task change", e );
            }
        }
    }

    protected synchronized void addTask ( final Task task )
    {
        if ( this.tasks.add ( task ) )
        {
            fireNotify ();
        }
    }

    protected synchronized void removeTask ( final Task task )
    {
        if ( this.tasks.remove ( task ) )
        {
            fireNotify ();
        }
    }

    protected synchronized void setTasks ( final Collection<Task> tasks )
    {
        this.tasks = new CopyOnWriteArraySet<> ( tasks );
        fireNotify ();
    }

    @Override
    public synchronized Collection<? extends Task> getTasks ()
    {
        return Collections.unmodifiableCollection ( this.tasks );
    }

    @Override
    public synchronized void addListener ( final TaskListener listener )
    {
        this.listeners.add ( listener );
    }

    @Override
    public synchronized void removeListener ( final TaskListener listener )
    {
        this.listeners.remove ( listener );
    }

}
