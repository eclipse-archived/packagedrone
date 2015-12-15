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
package org.eclipse.packagedrone.repo.channel.util;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;

import com.google.common.net.UrlEscapers;

/**
 * An abstract InterfaceExtender which only processes Channel instances
 */
public abstract class AbstractChannelInterfaceExtender implements InterfaceExtender
{
    protected boolean filterChannel ( final ChannelInformation channel )
    {
        return true;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final ChannelInformation channel = (ChannelInformation)object;
            if ( filterChannel ( channel ) )
            {
                return getChannelActions ( request, channel );
            }
        }
        return null;
    }

    protected List<MenuEntry> getChannelActions ( final HttpServletRequest request, final ChannelInformation channel )
    {
        return null;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final ChannelInformation channel = (ChannelInformation)object;
            if ( filterChannel ( channel ) )
            {
                return getChannelViews ( request, channel );
            }
        }
        return null;
    }

    protected List<MenuEntry> getChannelViews ( final HttpServletRequest request, final ChannelInformation channel )
    {
        return null;
    }

    protected static String escapePathSegment ( final String segment )
    {
        return UrlEscapers.urlPathSegmentEscaper ().escape ( segment );
    }
}
