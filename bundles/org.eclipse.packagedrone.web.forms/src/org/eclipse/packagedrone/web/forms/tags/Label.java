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

import javax.servlet.jsp.JspException;

public class Label extends FormTagSupport
{
    private static final long serialVersionUID = 1L;

    private String path;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<label" );
        writer.writeOptionalAttribute ( "for", this.path );
        writeDefaultAttributes ( writer );
        writer.write ( ">" );

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "</label>" );

        return EVAL_PAGE;
    }

    public void setPath ( final String path )
    {
        this.path = path;
    }
}
