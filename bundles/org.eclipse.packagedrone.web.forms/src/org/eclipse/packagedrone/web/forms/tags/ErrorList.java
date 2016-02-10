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

import java.util.List;

import javax.servlet.jsp.JspException;

import org.eclipse.packagedrone.web.controller.binding.BindingError;

public class ErrorList extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    @Override
    public int doStartTag () throws JspException
    {
        final List<BindingError> errors = getErrors ();

        if ( this.skipIfEmpty && errors.isEmpty () )
        {
            return SKIP_BODY;
        }

        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<ul" );
        writeDefaultAttributes ( writer );
        writer.write ( ">" );

        for ( final BindingError error : errors )
        {
            writer.write ( "<li>" );
            writer.writeEscaped ( error.getMessage () );
            writer.write ( "</li>" );
        }

        writer.write ( "</ul>" );

        return SKIP_BODY;
    }

    private boolean skipIfEmpty = true;

    public void setSkipIfEmpty ( final boolean skipIfEmpty )
    {
        this.skipIfEmpty = skipIfEmpty;
    }
}
