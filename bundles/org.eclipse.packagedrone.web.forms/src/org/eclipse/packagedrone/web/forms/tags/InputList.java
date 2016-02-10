/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.forms.tags;

import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

public class InputList extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private String path;

    private String var = "var";

    private Object[] items;

    private int index;

    private Object currentValue;

    @Override
    public int doStartTag () throws JspException
    {
        final Object value = getPathValue ( this.path );

        if ( value == null )
        {
            return Tag.SKIP_BODY;
        }

        if ( value.getClass ().isArray () )
        {
            this.items = (Object[])value;
        }
        else if ( value instanceof Collection<?> )
        {
            final int length = ( (Collection<?>)value ).size ();
            this.items = ( (Collection<?>)value ).toArray ( new Object[length] );
        }
        else
        {
            this.items = new Object[] { value };
        }

        this.index = -1;
        return pushNext ( true );
    }

    @Override
    public int doAfterBody () throws JspException
    {
        return pushNext ( false );
    }

    private int pushNext ( final boolean first )
    {
        this.index++;

        if ( this.index >= this.items.length )
        {
            return Tag.SKIP_BODY;
        }

        this.currentValue = this.items[this.index];
        this.pageContext.setAttribute ( this.var, this.currentValue );

        return first ? EVAL_BODY_INCLUDE : EVAL_BODY_AGAIN;
    }

    public Object getCurrentValue ()
    {
        return this.currentValue;
    }

    @Override
    public void setPath ( final String path )
    {
        this.path = path;
    }

    public String getPath ()
    {
        return this.path;
    }

    public void setVar ( final String var )
    {
        this.var = var;
    }

}
