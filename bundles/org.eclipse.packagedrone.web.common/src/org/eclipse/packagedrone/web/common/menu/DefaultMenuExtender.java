/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.common.menu;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.web.common.InterfaceExtender;

public class DefaultMenuExtender implements InterfaceExtender
{
    private final List<MenuEntry> entries = new LinkedList<> ();

    public DefaultMenuExtender ()
    {
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        return this.entries;
    }

    public void addEntry ( final MenuEntry entry )
    {
        this.entries.add ( entry );
    }

}
