/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH and others.
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

public class FormTagSupport extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private String cssClass;

    private Boolean spellcheck;

    protected void writeDefaultAttributes ( final WriterHelper writer ) throws JspException
    {
        writer.writeOptionalAttribute ( "class", this.cssClass );
        if ( this.spellcheck != null )
        {
            writer.writeOptionalAttribute ( "spellcheck", this.spellcheck );
        }
    }

    public void setCssClass ( final String cssClass )
    {
        this.cssClass = cssClass;
    }

    public void setSpellcheck ( final Boolean spellcheck )
    {
        this.spellcheck = spellcheck;
    }

    public void setSpellcheck ( final boolean spellcheck )
    {
        this.spellcheck = spellcheck;
    }
}
