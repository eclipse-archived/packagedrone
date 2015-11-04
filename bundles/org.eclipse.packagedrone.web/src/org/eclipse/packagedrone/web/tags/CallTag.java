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
package org.eclipse.packagedrone.web.tags;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class CallTag extends SimpleTagSupport implements DynamicAttributes
{
    private String name;

    private final Map<String, Object> data = new HashMap<> ();

    public void setName ( final String name )
    {
        this.name = name;
    }

    @Override
    public void doTag () throws JspException, IOException
    {
        final JspFragment body = (JspFragment)getJspContext ().getAttribute ( DefineTag.ATTR_PREFIX + this.name );

        if ( body == null )
        {
            throw new JspException ( String.format ( "Unable to find macro '%s'", this.name ) );
        }

        final JspContext ctx = body.getJspContext ();

        // set attributes to body context

        final Map<String, Object> oldEntries = new HashMap<> ( this.data.size () );

        for ( final Map.Entry<String, Object> entry : this.data.entrySet () )
        {
            oldEntries.put ( entry.getKey (), ctx.getAttribute ( entry.getKey (), PageContext.PAGE_SCOPE ) );
            ctx.setAttribute ( entry.getKey (), entry.getValue (), PageContext.PAGE_SCOPE );
        }

        // invoke

        body.invoke ( getJspContext ().getOut () );

        // set old values, so we don't clutter up the context for the next caller

        for ( final String key : this.data.keySet () )
        {
            final Object val = oldEntries.get ( key );
            if ( val == null )
            {
                ctx.removeAttribute ( key, PageContext.PAGE_SCOPE );
            }
            else
            {
                ctx.setAttribute ( key, val, PageContext.PAGE_SCOPE );
            }
        }
    }

    @Override
    public void setDynamicAttribute ( final String uri, final String key, final Object value ) throws JspException
    {
        this.data.put ( key, value );
    }

}
