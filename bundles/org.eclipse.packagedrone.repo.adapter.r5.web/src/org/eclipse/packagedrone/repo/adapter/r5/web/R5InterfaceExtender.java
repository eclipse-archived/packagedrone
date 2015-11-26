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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

        final Map<String, String> model = new HashMap<> ();
        model.put ( "channelId", channel.getId () );
        if ( channel.getName () != null && !channel.getName ().isEmpty () )
        {
            model.put ( "channelAlias", channel.getName () );
        }

        final List<MenuEntry> result = new LinkedList<> ();

        result.add ( new MenuEntry ( "R5 (by ID)", 10_000, new LinkTarget ( "/r5/{channelId}" ).expand ( model ), Modifier.LINK, null ) );
        result.add ( new MenuEntry ( "OBR (by ID)", 10_000, new LinkTarget ( "/obr/{channelId}" ).expand ( model ), Modifier.LINK, null ) );

        if ( model.containsKey ( "channelAlias" ) )
        {
            result.add ( new MenuEntry ( "R5 (by name)", 10_000, new LinkTarget ( "/r5/{channelAlias}" ).expand ( model ), Modifier.LINK, null ) );
            result.add ( new MenuEntry ( "OBR (by name)", 10_000, new LinkTarget ( "/obr/{channelAlias}" ).expand ( model ), Modifier.LINK, null ) );
        }

        return result;
    }
}
