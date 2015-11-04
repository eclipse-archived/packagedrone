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
package org.eclipse.packagedrone.repo.manage.todo.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.manage.todo.Task;
import org.eclipse.packagedrone.repo.manage.todo.TaskListener;
import org.eclipse.packagedrone.repo.manage.todo.TaskProvider;
import org.eclipse.packagedrone.repo.manage.todo.ToDoService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ToDoServiceImpl implements ToDoService
{

    private final BundleContext context;

    private final ServiceTracker<TaskProvider, TaskProvider> tracker;

    private final ServiceTrackerCustomizer<TaskProvider, TaskProvider> customizer = new ServiceTrackerCustomizer<TaskProvider, TaskProvider> () {

        @Override
        public void removedService ( final ServiceReference<TaskProvider> reference, final TaskProvider service )
        {
            removeService ( service );
            ToDoServiceImpl.this.context.ungetService ( reference );
        }

        @Override
        public void modifiedService ( final ServiceReference<TaskProvider> reference, final TaskProvider service )
        {
            removeService ( service );
            addService ( service );
        }

        @Override
        public TaskProvider addingService ( final ServiceReference<TaskProvider> reference )
        {
            final TaskProvider service = ToDoServiceImpl.this.context.getService ( reference );
            addService ( service );
            return service;
        }
    };

    public ToDoServiceImpl ()
    {
        this.context = FrameworkUtil.getBundle ( ToDoService.class ).getBundleContext ();
        this.tracker = new ServiceTracker<> ( this.context, TaskProvider.class, this.customizer );
    }

    private final Set<TaskProvider> providers = new CopyOnWriteArraySet<> ();

    private final TaskListener taskListener = new TaskListener () {

        @Override
        public void tasksChanged ( final Collection<? extends Task> tasks )
        {
            updateCache ();
        }
    };

    private volatile List<Task> allTasks;

    private volatile List<Task> openTasks;

    protected void addService ( final TaskProvider service )
    {
        this.providers.add ( service );
        service.addListener ( this.taskListener );

        updateCache ();
    }

    protected void removeService ( final TaskProvider service )
    {
        service.removeListener ( this.taskListener );
        this.providers.remove ( service );

        updateCache ();
    }

    protected void updateCache ()
    {
        this.allTasks = buildCache ();
        this.openTasks = filterOpen ( this.allTasks );
    }

    private List<Task> filterOpen ( final List<Task> tasks )
    {
        return Collections.unmodifiableList ( tasks.stream ().filter ( Task::isOpen ).collect ( Collectors.toList () ) );
    }

    protected List<Task> buildCache ()
    {
        final List<Task> result = new ArrayList<> ();

        for ( final TaskProvider provider : this.providers )
        {
            result.addAll ( provider.getTasks () );
        }

        Collections.sort ( result, Task.PRIORITY_COMPARATOR );

        return Collections.unmodifiableList ( result );
    }

    public void start ()
    {
        this.tracker.open ();
    }

    public void stop ()
    {
        this.tracker.close ();
    }

    @Override
    public List<Task> getOpenTasks ()
    {
        return this.openTasks;
    }

}
