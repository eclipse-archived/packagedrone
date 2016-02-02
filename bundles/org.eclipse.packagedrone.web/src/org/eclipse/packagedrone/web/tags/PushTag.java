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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class PushTag extends SimpleTagSupport
{
    private String name;

    static final String ATTR = PushTag.class.getName () + ".writers";

    public void setName ( final String name )
    {
        this.name = name;
    }

    @Override
    @SuppressWarnings ( "unchecked" )
    public void doTag () throws JspException, IOException
    {
        Map<String, StringWriter> writers = (Map<String, StringWriter>)getJspContext ().getAttribute ( ATTR, PageContext.REQUEST_SCOPE );
        if ( writers == null )
        {
            writers = new HashMap<> ();
            getJspContext ().setAttribute ( ATTR, writers, PageContext.REQUEST_SCOPE );
        }

        StringWriter writer = writers.get ( this.name );
        if ( writer == null )
        {
            writer = new StringWriter ();
            writers.put ( this.name, writer );
        }

        getJspBody ().invoke ( writer );
    }
}
