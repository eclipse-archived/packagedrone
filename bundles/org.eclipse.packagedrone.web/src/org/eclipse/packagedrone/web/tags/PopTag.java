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
package org.eclipse.packagedrone.web.tags;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class PopTag extends SimpleTagSupport
{
    private String name;

    public void setName ( final String name )
    {
        this.name = name;
    }

    @SuppressWarnings ( "unchecked" )
    @Override
    public void doTag () throws JspException, IOException
    {
        final Map<String, StringWriter> writers = (Map<String, StringWriter>)getJspContext ().getAttribute ( PushTag.ATTR, PageContext.REQUEST_SCOPE );

        if ( writers == null )
        {
            return;
        }

        final StringWriter writer = writers.remove ( this.name );
        if ( writer == null )
        {
            return;
        }

        getJspContext ().getOut ().write ( writer.toString () );
    }
}
