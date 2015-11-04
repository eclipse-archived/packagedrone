/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
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

public class Option extends OptionTagSupport
{
    private static final long serialVersionUID = 1L;

    private String value;

    private String label;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<option" );
        writer.writeAttribute ( "value", this.value );
        writer.writeFlagAttribute ( "selected", isSelected ( this.value ) );
        writer.write ( " >" );

        writer.writeEscaped ( this.label != null ? this.label : this.value );

        writer.write ( "</option>" );

        return SKIP_BODY;
    }

    public void setValue ( final String value )
    {
        this.value = value;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
    }

}
