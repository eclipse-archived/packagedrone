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

public class TextArea extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private Integer cols;

    private Integer rows;

    private String placeholder;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<textarea" );
        writer.writeAttribute ( "id", this.path );
        writer.writeAttribute ( "name", this.path );
        writer.writeOptionalAttribute ( "cols", this.cols );
        writer.writeOptionalAttribute ( "rows", this.rows );
        writer.writeOptionalAttribute ( "placeholder", this.placeholder );
        writeDefaultAttributes ( writer );
        writer.write ( " >" );

        writer.writeEscaped ( getPathValue ( this.path ) );

        writer.write ( "</textarea>" );

        return SKIP_BODY;
    }

    public void setCols ( final int cols )
    {
        this.cols = cols;
    }

    public void setRows ( final int rows )
    {
        this.rows = rows;
    }

    public void setPlaceholder ( final String placeholder )
    {
        this.placeholder = placeholder;
    }

}
