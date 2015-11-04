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

/**
 * A node in a ready to render menu
 */
public abstract class Node
{
    private final String label;

    private final String icon;

    private final String id;

    private final long badge;

    public Node ( final String id, final String label, final String icon, final long badge )
    {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.badge = badge;
    }

    public long getBadge ()
    {
        return this.badge;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public String getIcon ()
    {
        return this.icon;
    }

}
