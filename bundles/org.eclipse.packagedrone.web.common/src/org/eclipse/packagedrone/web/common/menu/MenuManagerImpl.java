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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class MenuManagerImpl
{
    private final ServiceTracker<InterfaceExtender, InterfaceExtender> tracker;

    public MenuManagerImpl ()
    {
        this.tracker = new ServiceTracker<InterfaceExtender, InterfaceExtender> ( FrameworkUtil.getBundle ( MenuManagerImpl.class ).getBundleContext (), InterfaceExtender.class, null );
        this.tracker.open ();
    }

    public void close ()
    {
        this.tracker.close ();
    }

    public Menu getMainMenu ( final HttpServletRequest request )
    {
        return getEntries ( ( i ) -> i.getMainMenuEntries ( request ) );
    }

    public Menu getActions ( final HttpServletRequest request, final Object context )
    {
        return getEntries ( ( i ) -> i.getActions ( request, context ) );
    }

    public Menu getViews ( final HttpServletRequest request, final Object context )
    {
        return getEntries ( ( i ) -> i.getViews ( request, context ) );
    }

    protected Menu getEntries ( final Function<InterfaceExtender, List<MenuEntry>> func )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        for ( final InterfaceExtender me : this.tracker.getTracked ().values () )
        {
            final List<MenuEntry> actions = func.apply ( me );
            if ( actions != null )
            {
                result.addAll ( actions );
            }
        }

        return convert ( result );
    }

    protected Menu convert ( List<MenuEntry> entries )
    {
        if ( entries == null )
        {
            return null;
        }

        entries = condenseCategories ( entries );
        Collections.sort ( entries );

        final List<Node> nodes = new LinkedList<> ();

        List<Node> currentNodes = nodes;
        String currentCategory = null;

        for ( final MenuEntry entry : entries )
        {
            if ( entry.getCategory () == null )
            {
                // main menu entry
                currentNodes = nodes;
                currentCategory = null;
            }
            else
            {
                // sub menu entry
                if ( currentCategory == null || !currentCategory.equals ( entry.getCategory () ) )
                {
                    // switch category
                    currentNodes = new LinkedList<> ();
                    currentCategory = entry.getCategory ();
                    nodes.add ( new SubMenu ( UUID.randomUUID ().toString (), entry.getCategory (), currentNodes ) );
                }
                else
                {
                    // same category
                }
            }
            currentNodes.add ( convertEntry ( entry ) );
        }

        return new Menu ( nodes );
    }

    private List<MenuEntry> condenseCategories ( final List<MenuEntry> entries )
    {
        final Map<String, Integer> map = new HashMap<> ();
        for ( final MenuEntry entry : entries )
        {
            final String cat = entry.getCategory ();
            if ( cat == null )
            {
                // entry has not category
                continue;
            }

            final Integer prio = map.get ( cat );
            if ( prio == null && entry.getCategoryOrder () != Integer.MAX_VALUE )
            {
                // we don't yet have a category and the cat order is not "unspecified"
                map.put ( cat, entry.getCategoryOrder () );
            }
            else if ( prio != null && entry.getCategoryOrder () < prio )
            {
                // overwrite with higher priority
                map.put ( cat, entry.getCategoryOrder () );
            }
        }

        // we should have a map of categories priorities now

        final List<MenuEntry> result = new ArrayList<> ( entries.size () );

        for ( final MenuEntry entry : entries )
        {
            final String cat = entry.getCategory ();
            if ( cat == null )
            {
                // no category, just copy
                result.add ( entry );
                continue;
            }

            final Integer prio = map.get ( cat );
            if ( prio == null )
            {
                // no recorded priority, copy
                result.add ( entry );
                // and record priority
                map.put ( cat, entry.getCategoryOrder () );
            }
            else
            {
                // recorded priority, create new entry with recorded priority
                result.add ( new MenuEntry ( entry.getCategory (), prio, entry.getLabel (), entry.getEntryOrder (), entry.getTarget (), entry.getModifier (), entry.getIcon (), entry.isNewWindow (), entry.getBadge () ) );
            }
        }

        return result;
    }

    private Entry convertEntry ( final MenuEntry entry )
    {
        return new Entry ( UUID.randomUUID ().toString (), entry.getLabel (), entry.getTarget (), entry.getModifier (), entry.getIcon (), entry.isNewWindow (), entry.getBadge (), entry.getModal () );
    }

}
