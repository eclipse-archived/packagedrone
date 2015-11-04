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
package org.eclipse.packagedrone.web.common;

public class Button
{
    private final String label;

    private final String icon;

    private final Modifier modifier;

    public Button ( final String label )
    {
        this ( label, null, null );
    }

    public Button ( final String label, final String icon, final Modifier modifier )
    {
        this.label = label;
        this.icon = icon;
        this.modifier = modifier != null ? modifier : Modifier.DEFAULT;
    }

    public String getIcon ()
    {
        return this.icon;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public Modifier getModifier ()
    {
        return this.modifier;
    }
}
