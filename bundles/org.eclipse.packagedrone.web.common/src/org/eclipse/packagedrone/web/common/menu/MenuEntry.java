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

import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.common.Modifier;

import com.google.common.html.HtmlEscapers;

/**
 * A menu entry from an extension point
 */
public class MenuEntry implements Comparable<MenuEntry>
{
    private String category;

    private String label;

    private LinkTarget target;

    private Modifier modifier;

    private int categoryOrder;

    private int entryOrder;

    private boolean newWindow;

    private String icon;

    private Modal modal;

    private long badge;

    public MenuEntry ( final String category, final int categoryOrder, final String label, final int entryOrder, final LinkTarget target, final Modifier modifier, final String icon )
    {
        this ( category, categoryOrder, label, entryOrder, target, modifier, icon, false, 0 );
    }

    public MenuEntry ( final String category, final int categoryOrder, final String label, final int entryOrder, final LinkTarget target, final Modifier modifier, final String icon, final boolean newWindow, final long badge )
    {
        this.category = category;
        this.categoryOrder = category != null ? categoryOrder : entryOrder;
        this.label = label;
        this.entryOrder = category != null ? entryOrder : 0;
        this.target = target;
        this.modifier = modifier;
        this.icon = icon;
        this.newWindow = newWindow;
        this.badge = badge;
    }

    public MenuEntry ( final String label, final int entryOrder, final LinkTarget target, final Modifier modifier, final String icon )
    {
        this.category = null;
        this.categoryOrder = entryOrder;
        this.label = label;
        this.entryOrder = 0;
        this.target = target;
        this.modifier = modifier;
        this.icon = icon;
        this.newWindow = false;
    }

    public String getIcon ()
    {
        return this.icon;
    }

    public boolean isNewWindow ()
    {
        return this.newWindow;
    }

    public String getCategory ()
    {
        return this.category;
    }

    public int getCategoryOrder ()
    {
        return this.categoryOrder;
    }

    public int getEntryOrder ()
    {
        return this.entryOrder;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public LinkTarget getTarget ()
    {
        return this.target;
    }

    public Modifier getModifier ()
    {
        return this.modifier;
    }

    protected String getMainLabel ()
    {
        if ( this.category != null )
        {
            return this.category;
        }
        return this.label;
    }

    public MenuEntry setCategory ( final String category )
    {
        this.category = category;
        return this;
    }

    public MenuEntry setLabel ( final String label )
    {
        this.label = label;
        return this;
    }

    public MenuEntry setTarget ( final LinkTarget target )
    {
        this.target = target;
        return this;
    }

    public MenuEntry setModifier ( final Modifier modifier )
    {
        this.modifier = modifier;
        return this;
    }

    public MenuEntry setCategoryOrder ( final int categoryOrder )
    {
        this.categoryOrder = categoryOrder;
        return this;
    }

    public MenuEntry setEntryOrder ( final int entryOrder )
    {
        this.entryOrder = entryOrder;
        return this;
    }

    public MenuEntry setNewWindow ( final boolean newWindow )
    {
        this.newWindow = newWindow;
        return this;
    }

    public MenuEntry setIcon ( final String icon )
    {
        this.icon = icon;
        return this;
    }

    public long getBadge ()
    {
        return this.badge;
    }

    public MenuEntry setBadge ( final long badge )
    {
        this.badge = badge;
        return this;
    }

    public MenuEntry setModal ( final Modal modal )
    {
        this.modal = modal;
        return this;
    }

    public Modal getModal ()
    {
        return this.modal;
    }

    public MenuEntry makeModalMessage ( final String title, final String message )
    {
        this.modal = new Modal ( title, new FunctionalButton ( ButtonFunction.CLOSE, "Close" ), new FunctionalButton ( ButtonFunction.SUBMIT, this.label, this.icon, this.modifier ) );
        this.modal.setBody ( "<p>" + HtmlEscapers.htmlEscaper ().escape ( message ) + "</p>" );
        return this;
    }

    @Override
    public int compareTo ( final MenuEntry o )
    {
        // first by category
        int rc = Integer.compare ( this.categoryOrder, o.categoryOrder );
        if ( rc != 0 )
        {
            return rc;
        }

        // then by entry
        rc = Integer.compare ( this.entryOrder, o.entryOrder );
        if ( rc != 0 )
        {
            return rc;
        }

        // last by label
        return getMainLabel ().compareTo ( o.getMainLabel () );
    }

    @Override
    public String toString ()
    {
        return String.format ( "[Menu: %s@%s/%s@%s - %s]", this.category, this.categoryOrder, this.label, this.entryOrder, this.target );
    }

}
