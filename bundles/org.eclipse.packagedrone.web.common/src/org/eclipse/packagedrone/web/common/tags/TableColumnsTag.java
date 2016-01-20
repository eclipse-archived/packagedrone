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
import javax.servlet.jsp.tagext.TagSupport;

import org.eclipse.packagedrone.web.common.table.TableColumn;
import org.eclipse.packagedrone.web.common.table.TableExtension;

public class TableColumnsTag extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private int start = Integer.MIN_VALUE;

    private int end = Integer.MAX_VALUE;

    private LinkedList<TableColumn> columns;

    private String var;

    public void setStart ( final int start )
    {
        this.start = start;
    }

    public void setEnd ( final int end )
    {
        this.end = end;
    }

    public void setVar ( final String var )
    {
        this.var = var;
    }

    protected TableExtension getExtension ()
    {
        final TableExtenderTag parent = (TableExtenderTag)findAncestorWithClass ( this, TableExtenderTag.class );
        if ( parent == null )
        {
            return null;
        }

        return parent.getExtension ();
    }

    @Override
    public int doStartTag () throws JspException
    {
        final TableExtension extension = getExtension ();
        if ( this.var == null || extension == null )
        {
            return Tag.SKIP_BODY;
        }

        this.columns = new LinkedList<> ( extension.getColumns ( this.start, this.end ) );

        return pollNext ();
    }

    @Override
    public int doAfterBody () throws JspException
    {
        return pollNext ();
    }

    private int pollNext ()
    {
        if ( !this.columns.isEmpty () )
        {
            this.pageContext.setAttribute ( this.var, this.columns.pollFirst () );
            return Tag.EVAL_BODY_INCLUDE;
        }
        else
        {
            return Tag.SKIP_BODY;
        }
    }

}
