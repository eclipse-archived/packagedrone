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

public class Select extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private String path;

    private Object selectedValue;

    private boolean multiple;

    public void setMultiple ( final boolean multiple )
    {
        this.multiple = multiple;
    }

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<select" );
        writer.writeOptionalAttribute ( "id", this.path );
        writer.writeOptionalAttribute ( "name", this.path );
        writer.writeFlagAttribute ( "multiple", this.multiple );
        writeDefaultAttributes ( writer );
        writer.write ( " >" );

        this.selectedValue = getPathValue ( this.path );

        return EVAL_BODY_INCLUDE;
    }

    public Object getSelectedValue ()
    {
        return this.selectedValue;
    }

    @Override
    public int doEndTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "</select>" );

        return EVAL_PAGE;
    }

    @Override
    public void setPath ( final String path )
    {
        this.path = path;
    }
}
