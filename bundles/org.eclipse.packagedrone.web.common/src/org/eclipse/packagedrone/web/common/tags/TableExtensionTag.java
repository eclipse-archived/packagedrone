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
package org.eclipse.packagedrone.web.common.tags;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.eclipse.packagedrone.web.common.table.TableColumnProvider;
import org.eclipse.packagedrone.web.common.table.TableDescriptor;

public class TableExtensionTag extends TableChildTag
{
    private static final long serialVersionUID = 1L;

    @Override
    public int doStartTag () throws JspException
    {
        final TableRowTag rowTag = (TableRowTag)findAncestorWithClass ( this, TableRowTag.class );
        if ( rowTag == null )
        {
            return Tag.SKIP_BODY;
        }

        final TableColumnProvider provider = rowTag.getCurrentProvider ();
        if ( provider == null )
        {
            return Tag.SKIP_BODY;
        }

        final TableDescriptor descriptor = rowTag.getDescriptor ();
        if ( descriptor == null )
        {
            return Tag.SKIP_BODY;
        }

        final Object item = rowTag.getItem ();

        final PrintWriter pw = new PrintWriter ( this.pageContext.getOut () );

        try
        {
            provider.provideContent ( descriptor, item, pw );
        }
        catch ( final IOException e )
        {
            throw new JspException ( e );
        }

        return Tag.SKIP_BODY;
    }

}
