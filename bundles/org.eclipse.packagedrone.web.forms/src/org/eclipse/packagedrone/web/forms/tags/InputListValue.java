/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
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
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

public class InputListValue extends TagSupport
{
    private static final long serialVersionUID = 1L;

    @Override
    public int doStartTag () throws JspException
    {
        final InputList parent = (InputList)findAncestorWithClass ( this, InputList.class );

        if ( parent == null )
        {
            throw new JspException ( "Missing parent 'inputList' element" );
        }

        final Object value = parent.getCurrentValue ();
        if ( value == null )
        {
            return Tag.SKIP_BODY;
        }

        final WriterHelper wh = new WriterHelper ( this.pageContext );
        wh.write ( "<input type=\"hidden\"" );
        wh.writeAttribute ( "name", parent.getPath () );
        wh.writeAttribute ( "value", value );
        wh.write ( " />" );

        return Tag.SKIP_BODY;
    }
}
