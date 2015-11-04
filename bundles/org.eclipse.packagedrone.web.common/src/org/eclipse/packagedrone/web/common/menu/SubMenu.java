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

import java.util.Collections;
import java.util.List;

import org.eclipse.packagedrone.web.common.Modifier;

public class SubMenu extends Node
{
    private final List<Node> nodes;

    private final Modifier modifier;

    public SubMenu ( final String id, final String label, final List<Node> nodes )
    {
        this ( id, label, nodes, Modifier.DEFAULT );
    }

    public SubMenu ( final String id, final String label, final List<Node> nodes, final Modifier modifier )
    {
        super ( id, label, null, 0 );
        this.nodes = Collections.unmodifiableList ( nodes );
        this.modifier = modifier;
    }

    public List<Node> getNodes ()
    {
        return this.nodes;
    }

    public Modifier getModifier ()
    {
        return this.modifier;
    }
}
