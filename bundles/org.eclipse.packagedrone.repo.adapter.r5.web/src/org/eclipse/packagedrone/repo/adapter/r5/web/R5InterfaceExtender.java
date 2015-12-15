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
package org.eclipse.packagedrone.repo.adapter.r5.web;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.adapter.r5.R5RepositoryAspectFactory;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.util.AbstractChannelInterfaceExtender;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;

public class R5InterfaceExtender extends AbstractChannelInterfaceExtender
{
    @Override
    protected List<MenuEntry> getChannelActions ( final HttpServletRequest request, final ChannelInformation channel )
    {
        if ( !channel.hasAspect ( R5RepositoryAspectFactory.ID ) )
        {
            return null;
        }

        final List<MenuEntry> result = new LinkedList<> ();

        result.add ( new MenuEntry ( "R5/OBR", 10_000, "R5 (by ID)", 10_000, new LinkTarget ( "/r5/" + escapePathSegment ( channel.getId () ) ), Modifier.LINK, null ) );
        result.add ( new MenuEntry ( "R5/OBR", 10_000, "OBR (by ID)", 11_000, new LinkTarget ( "/obr/" + escapePathSegment ( channel.getId () ) ), Modifier.LINK, null ) );

        int i = 1;
        for ( final String name : channel.getNames () )
        {
            result.add ( new MenuEntry ( "R5/OBR", 10_000, String.format ( "R5 (name: %s)", name ), 10_000 + 1, new LinkTarget ( "/r5/" + escapePathSegment ( name ) ), Modifier.LINK, null ) );
            result.add ( new MenuEntry ( "R5/OBR", 10_000, String.format ( "OBR (name: %s)", name ), 11_000 + 1, new LinkTarget ( "/obr/" + escapePathSegment ( name ) ), Modifier.LINK, null ) );
            i++;
        }

        return result;
    }
}
