/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.forms.tags;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.eclipse.packagedrone.web.controller.binding.BindingError;

public class Errors extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private Iterator<BindingError> iter;

    private String var = "error";

    @Override
    public int doStartTag () throws JspException
    {
        final List<BindingError> errors = getErrors ();
        this.iter = errors.iterator ();

        if ( !this.iter.hasNext () )
        {
            return SKIP_BODY;
        }

        this.pageContext.setAttribute ( this.var, this.iter.next () );

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doAfterBody () throws JspException
    {
        if ( this.iter.hasNext () )
        {
            this.pageContext.setAttribute ( this.var, this.iter.next () );
            return EVAL_BODY_AGAIN;
        }
        else
        {
            return SKIP_BODY;
        }
    }

    @Override
    public int doEndTag () throws JspException
    {
        return EVAL_PAGE;
    }

    public void setVar ( final String var )
    {
        this.var = var;
    }
}
