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

/**
 * A ready to render menu <br/>
 * In comparison to the MenuEntry, this class is a ready to render menu.
 */
public class Menu
{
    private final List<Node> nodes;

    public Menu ( final List<Node> nodes )
    {
        this.nodes = Collections.unmodifiableList ( nodes );
    }

    public List<Node> getNodes ()
    {
        return this.nodes;
    }
}
