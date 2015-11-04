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
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
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
        final Map<String, LinkedList<JspFragment>> stacks = (Map<String, LinkedList<JspFragment>>)getJspContext ().getAttribute ( PushTag.ATTR, PageContext.REQUEST_SCOPE );

        if ( stacks == null )
        {
            return;
        }

        final LinkedList<JspFragment> stack = stacks.get ( this.name );
        if ( stack == null )
        {
            return;
        }

        for ( final JspFragment fragment : stack )
        {
            fragment.invoke ( getJspContext ().getOut () );
        }

        stack.clear ();
    }
}
