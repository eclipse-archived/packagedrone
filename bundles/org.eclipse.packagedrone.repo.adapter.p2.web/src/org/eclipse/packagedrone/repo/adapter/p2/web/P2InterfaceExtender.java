/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.p2.web;

import static org.eclipse.packagedrone.repo.channel.util.RepositoryLinks.fillRepoLinks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.adapter.p2.aspect.P2RepositoryAspect;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.util.AbstractChannelInterfaceExtender;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;

public class P2InterfaceExtender extends AbstractChannelInterfaceExtender
{
    public static final String P2_METADATA_ASPECT_ID = "p2.metadata";

    private static final LinkTarget P2_LINK_TEMPLATE = new LinkTarget ( "/p2/{idOrName}" );

    @Override
    protected List<MenuEntry> getChannelActions ( final HttpServletRequest request, final ChannelInformation channel )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        repoActions ( request, channel, result );
        metaDataActions ( request, channel, result );

        return result;
    }

    private void metaDataActions ( final HttpServletRequest request, final ChannelInformation channel, final List<MenuEntry> result )
    {
        if ( !channel.hasAspect ( P2_METADATA_ASPECT_ID ) )
        {
            return;
        }

        if ( request.isUserInRole ( "MANAGER" ) )
        {
            result.add ( new MenuEntry ( "Edit", Integer.MAX_VALUE, "P2 Meta Data Generator", 10_000, new LinkTarget ( String.format ( "/p2.metadata/%s/edit", escapePathSegment ( channel.getId () ) ) ), Modifier.DEFAULT, null, false, 0 ) );
        }
    }

    private void repoActions ( final HttpServletRequest request, final ChannelInformation channel, final List<MenuEntry> result )
    {
        if ( !channel.hasAspect ( P2RepositoryAspect.ID ) )
        {
            return;
        }

        fillRepoLinks ( channel, result, "P2", 9_000, P2_LINK_TEMPLATE );

        if ( request.isUserInRole ( "MANAGER" ) )
        {
            result.add ( new MenuEntry ( "Edit", Integer.MAX_VALUE, "P2 Repository Information", 10_000, new LinkTarget ( String.format ( "/p2.repo/%s/edit", escapePathSegment ( channel.getId () ) ) ), Modifier.DEFAULT, null, false, 0 ) );
        }
    }

    @Override
    protected List<MenuEntry> getChannelViews ( final HttpServletRequest request, final ChannelInformation channel )
    {
        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "channelId", channel.getId () );

        final List<MenuEntry> result = new LinkedList<> ();

        if ( channel.hasAspect ( P2RepositoryAspect.ID ) )
        {
            result.add ( new MenuEntry ( "P2", 5_000, "Repository", 1000, new LinkTarget ( "/p2.repo/{channelId}/info" ).expand ( model ), Modifier.INFO, null, false, 0 ) );
        }

        if ( channel.hasAspect ( P2_METADATA_ASPECT_ID ) )
        {
            result.add ( new MenuEntry ( "P2", 5_000, "Meta Data Generation", 500, new LinkTarget ( "/p2.metadata/{channelId}/info" ).expand ( model ), Modifier.INFO, null, false, 0 ) );
        }

        return result;
    }
}
