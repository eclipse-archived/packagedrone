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
package org.eclipse.packagedrone.web.forms.tags;

import javax.servlet.jsp.JspException;

public class Radio extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private boolean disabled;

    private boolean readonly;

    private String value;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<input" );
        writer.writeOptionalAttribute ( "id", this.id );
        writer.writeAttribute ( "name", this.path );
        writer.writeAttribute ( "value", this.value );

        final Object value = getPathValue ( this.path );
        if ( value != null )
        {
            if ( value.toString ().equals ( this.value ) )
            {
                writer.writeFlagAttribute ( "checked" );
            }
        }

        writer.writeOptionalAttribute ( "type", "radio" );
        writer.writeFlagAttribute ( "disabled", this.disabled );
        writer.writeFlagAttribute ( "readonly", this.readonly );
        writeDefaultAttributes ( writer );
        writer.write ( " />" );

        return SKIP_BODY;
    }

    public void setDisabled ( final boolean disabled )
    {
        this.disabled = disabled;
    }

    public void setReadonly ( final boolean readonly )
    {
        this.readonly = readonly;
    }

    public void setValue ( final String value )
    {
        this.value = value;
    }

}
