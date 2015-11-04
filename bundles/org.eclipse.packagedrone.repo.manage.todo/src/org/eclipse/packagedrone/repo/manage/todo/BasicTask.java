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

import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.common.Button;
import org.eclipse.packagedrone.web.common.Modifier;

public class BasicTask implements Task
{
    private State state;

    private final String title;

    private final String description;

    private final LinkTarget target;

    private final int priority;

    private final RequestMethod targetRequestMethod;

    private final Button button;

    public BasicTask ( final String title, final int priority, final String description, final LinkTarget target )
    {
        this ( title, priority, description, target, null, null );
    }

    public BasicTask ( final String title, final int priority, final String description, final LinkTarget target, final RequestMethod method, final Button button )
    {
        this.state = State.TODO;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.target = target;
        this.targetRequestMethod = method == null ? RequestMethod.GET : method;
        this.button = button == null ? new Button ( "Link", null, Modifier.LINK ) : button;
    }

    @Override
    public RequestMethod getTargetRequestMethod ()
    {
        return this.targetRequestMethod;
    }

    @Override
    public Button getButton ()
    {
        return this.button;
    }

    public void setState ( final State state )
    {
        this.state = state;
    }

    @Override
    public State getState ()
    {
        return this.state;
    }

    @Override
    public String getTitle ()
    {
        return this.title;
    }

    @Override
    public String getDescription ()
    {
        return this.description;
    }

    @Override
    public LinkTarget getTarget ()
    {
        return this.target;
    }

    @Override
    public int getPriority ()
    {
        return this.priority;
    }
}
