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

import java.util.Comparator;

import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.common.Button;
import org.eclipse.packagedrone.web.common.Modifier;

public interface Task
{
    public static enum State
    {
        TODO,
        FAILED,
        DONE;
    }

    public State getState ();

    public String getTitle ();

    public String getDescription ();

    /**
     * Get an optional link target
     *
     * @return the link target or <code>null</code>
     */
    public LinkTarget getTarget ();

    public default RequestMethod getTargetRequestMethod ()
    {
        return RequestMethod.GET;
    }

    public static final Button DEFAULT_BUTTON = new Button ( "Link", null, Modifier.LINK );

    public default Button getButton ()
    {
        return DEFAULT_BUTTON;
    }

    public int getPriority ();

    public default boolean isDone ()
    {
        return getState () == State.DONE;
    }

    public default boolean isOpen ()
    {
        return getState () != State.DONE;
    }

    public static final Comparator<Task> PRIORITY_COMPARATOR = PriorityComparator.INSTANCE;

}
