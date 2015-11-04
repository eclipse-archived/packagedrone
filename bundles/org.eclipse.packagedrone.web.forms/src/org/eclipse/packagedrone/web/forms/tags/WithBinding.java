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
package org.eclipse.packagedrone.web.forms.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.eclipse.packagedrone.web.controller.binding.BindingResult;

public class WithBinding extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private String var = "result";

    private String command = "";

    private Object oldValue;

    public void setVar ( final String var )
    {
        this.var = var;
    }

    public void setCommand ( final String command )
    {
        this.command = command;
    }

    @Override
    public int doStartTag () throws JspException
    {
        this.oldValue = this.pageContext.getAttribute ( this.var );
        this.pageContext.setAttribute ( this.var, getResult () );
        return EVAL_BODY_INCLUDE;
    }

    private Object getResult ()
    {
        final Object bindingResult = this.pageContext.getAttribute ( BindingResult.ATTRIBUTE_NAME );

        if ( bindingResult instanceof BindingResult )
        {
            return ( (BindingResult)bindingResult ).getChild ( this.command );
        }
        return null;
    }

    @Override
    public int doAfterBody () throws JspException
    {
        if ( this.oldValue == null )
        {
            this.pageContext.removeAttribute ( this.var );
        }
        else
        {
            this.pageContext.setAttribute ( this.var, this.oldValue );
        }

        return EVAL_PAGE;
    }
}
