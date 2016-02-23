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
package org.eclipse.packagedrone.repo.adapter.rpm.web;

import static org.eclipse.packagedrone.repo.channel.util.RepositoryLinks.fillRepoLinks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.adapter.rpm.Constants;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.util.AbstractChannelInterfaceExtender;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;

public class YumInterfaceExtender extends AbstractChannelInterfaceExtender
{
    private static final LinkTarget YUM_LINK_TEMPLATE = new LinkTarget ( "/yum/{idOrName}" );

    @Override
    protected boolean filterChannel ( final ChannelInformation channel )
    {
        return channel.hasAspect ( Constants.YUM_ASPECT_ID );
    }

    @Override
    protected List<MenuEntry> getChannelActions ( final HttpServletRequest request, final ChannelInformation channel )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        fillRepoLinks ( channel, result, "YUM", 6_000, YUM_LINK_TEMPLATE );

        return result;
    }

    @Override
    protected List<MenuEntry> getChannelViews ( final HttpServletRequest request, final ChannelInformation channel )
    {
        final Map<String, String> model = new HashMap<> ( 1 );
        model.put ( "channelId", channel.getId () );

        final List<MenuEntry> result = new LinkedList<> ();

        result.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "YUM", 6_000, new LinkTarget ( "/ui/yum/{channelId}/help" ).expand ( model ), Modifier.DEFAULT, "info-sign" ) );
        result.add ( new MenuEntry ( "YUM", 6_000, new LinkTarget ( "/ui/yum/{channelId}/edit" ).expand ( model ), Modifier.DEFAULT, null ) );

        return result;
    }

}
