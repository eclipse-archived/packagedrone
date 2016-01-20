/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.common.tags;

import java.util.LinkedList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.eclipse.packagedrone.web.common.table.TableColumnProvider;
import org.eclipse.packagedrone.web.common.table.TableDescriptor;
import org.eclipse.packagedrone.web.common.table.TableExtension;

public class TableRowTag extends TableChildTag
{
    private static final long serialVersionUID = 1L;

    private int start = Integer.MIN_VALUE;

    private int end = Integer.MAX_VALUE;

    private Object item;

    private LinkedList<TableColumnProvider> providers;

    private TableColumnProvider currentProvider;

    private TableDescriptor descriptor;

    public void setStart ( final int start )
    {
        this.start = start;
    }

    public void setEnd ( final int end )
    {
        this.end = end;
    }

    public void setItem ( final Object item )
    {
        this.item = item;
    }

    public TableColumnProvider getCurrentProvider ()
    {
        return this.currentProvider;
    }

    public TableDescriptor getDescriptor ()
    {
        return this.descriptor;
    }

    public Object getItem ()
    {
        return this.item;
    }

    @Override
    public int doStartTag () throws JspException
    {
        final Object item = this.item;

        final TableExtension extension = getExtension ();
        if ( item == null || extension == null )
        {
            return Tag.SKIP_BODY;
        }

        this.descriptor = extension.geTableDescriptor ();
        this.providers = new LinkedList<> ( extension.getProviders ( this.start, this.end ) );

        return pollNext ();
    }

    @Override
    public int doAfterBody () throws JspException
    {
        return pollNext ();
    }

    private int pollNext ()
    {
        if ( this.providers == null || this.providers.isEmpty () )
        {
            return Tag.SKIP_BODY;
        }

        this.currentProvider = this.providers.pollFirst ();

        return Tag.EVAL_BODY_INCLUDE;
    }

}
