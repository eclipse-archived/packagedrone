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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class PushTag extends SimpleTagSupport
{
    private String name;

    public static String ATTR = PushTag.class.getName () + ".stacks";

    public void setName ( final String name )
    {
        this.name = name;
    }

    @Override
    @SuppressWarnings ( "unchecked" )
    public void setJspBody ( final JspFragment jspBody )
    {
        super.setJspBody ( jspBody );

        Map<String, LinkedList<JspFragment>> stacks = (Map<String, LinkedList<JspFragment>>)getJspContext ().getAttribute ( ATTR, PageContext.REQUEST_SCOPE );
        if ( stacks == null )
        {
            stacks = new HashMap<> ();
            getJspContext ().setAttribute ( ATTR, stacks, PageContext.REQUEST_SCOPE );
        }

        LinkedList<JspFragment> stack = stacks.get ( this.name );
        if ( stack == null )
        {
            stack = new LinkedList<> ();
            stacks.put ( this.name, stack );
        }

        stack.add ( jspBody );
    }
}
