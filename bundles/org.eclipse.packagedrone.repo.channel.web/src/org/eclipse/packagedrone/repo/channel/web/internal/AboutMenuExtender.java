/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.web.internal;

import static org.eclipse.packagedrone.web.LinkTarget.createFromController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;

public class AboutMenuExtender implements InterfaceExtender
{
    private final List<MenuEntry> entries;

    public AboutMenuExtender ()
    {
        final List<MenuEntry> entries = new ArrayList<> ( 4 );
        entries.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "About", Integer.MAX_VALUE, createFromController ( AboutController.class, "about" ), null, null, true, 0 ) );
        entries.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "Blog", 1_000, new LinkTarget ( "https://packagedrone.org" ), null, null, true, 0 ) );
        entries.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "Project", 2_000, new LinkTarget ( "https://eclipse.org/package-drone" ), null, null, true, 0 ) );
        entries.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "Wiki", 3_000, new LinkTarget ( "https://wiki.eclipse.org/PackageDrone" ), null, null, true, 0 ) );
        this.entries = Collections.unmodifiableList ( entries );
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        return this.entries;
    }
}
