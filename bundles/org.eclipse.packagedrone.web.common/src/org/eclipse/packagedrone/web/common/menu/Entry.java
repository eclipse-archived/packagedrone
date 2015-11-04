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
package org.eclipse.packagedrone.web.common.menu;

import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.common.Modifier;

public class Entry extends Node
{
    private final LinkTarget target;

    private final Modifier modifier;

    private final boolean newWindow;

    private final Modal modal;

    public Entry ( final String id, final String label, final LinkTarget target, final Modifier modifier, final String icon, final boolean newWindow, final long badge, final Modal modal )
    {
        super ( id, label, icon, badge );
        this.target = target;
        this.modifier = modifier;

        this.newWindow = newWindow;

        this.modal = modal;
    }

    public Modifier getModifier ()
    {
        return this.modifier;
    }

    public LinkTarget getTarget ()
    {
        return this.target;
    }

    public boolean isNewWindow ()
    {
        return this.newWindow;
    }

    public Modal getModal ()
    {
        return this.modal;
    }
}
