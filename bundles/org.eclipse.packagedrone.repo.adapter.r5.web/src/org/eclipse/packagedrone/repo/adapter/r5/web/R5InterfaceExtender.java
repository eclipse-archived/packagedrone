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
package org.eclipse.packagedrone.repo.adapter.r5.web;

import static org.eclipse.packagedrone.repo.channel.util.RepositoryLinks.fillRepoLinks;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.adapter.r5.R5RepositoryAspectFactory;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.util.AbstractChannelInterfaceExtender;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;

public class R5InterfaceExtender extends AbstractChannelInterfaceExtender
{
    private static final LinkTarget R5_LINK_TEMPLATE = new LinkTarget ( "/r5/{idOrName}" );

    private static final LinkTarget OBR_LINK_TEMPLATE = new LinkTarget ( "/obr/{idOrName}" );

    @Override
    protected List<MenuEntry> getChannelActions ( final HttpServletRequest request, final ChannelInformation channel )
    {
        if ( !channel.hasAspect ( R5RepositoryAspectFactory.ID ) )
        {
            return null;
        }

        final List<MenuEntry> result = new LinkedList<> ();

        fillRepoLinks ( channel, result, "R5/OBR", 10_000, "R5", 10_000, R5_LINK_TEMPLATE );
        fillRepoLinks ( channel, result, "R5/OBR", 10_000, "OBR", 11_000, OBR_LINK_TEMPLATE );

        return result;
    }

}
