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

public class Input extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private String type;

    private boolean disabled;

    private boolean readonly;

    private boolean required;

    private String placeholder;

    private Integer min;

    private Integer max;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<input" );
        writer.writeAttribute ( "id", this.path );
        writer.writeAttribute ( "name", this.path );
        writer.writeOptionalAttribute ( "value", getPathValue ( this.path ) );
        writer.writeOptionalAttribute ( "type", this.type );
        writer.writeOptionalAttribute ( "placeholder", this.placeholder );
        writer.writeOptionalAttribute ( "min", this.min );
        writer.writeOptionalAttribute ( "max", this.max );
        writer.writeFlagAttribute ( "disabled", this.disabled );
        writer.writeFlagAttribute ( "readonly", this.readonly );
        writer.writeFlagAttribute ( "required", this.required );
        writeDefaultAttributes ( writer );
        writer.write ( " />" );

        return SKIP_BODY;
    }

    public void setType ( final String type )
    {
        this.type = type;
    }

    public void setDisabled ( final boolean disabled )
    {
        this.disabled = disabled;
    }

    public void setReadonly ( final boolean readonly )
    {
        this.readonly = readonly;
    }

    public void setRequired ( final boolean required )
    {
        this.required = required;
    }

    public void setPlaceholder ( final String placeholder )
    {
        this.placeholder = placeholder;
    }

    public void setMin ( final Integer min )
    {
        this.min = min;
    }

    public void setMax ( final Integer max )
    {
        this.max = max;
    }

}
