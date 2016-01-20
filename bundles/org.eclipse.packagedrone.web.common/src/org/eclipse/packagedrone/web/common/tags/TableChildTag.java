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

import javax.servlet.jsp.tagext.TagSupport;

import org.eclipse.packagedrone.web.common.table.TableExtension;

public abstract class TableChildTag extends TagSupport
{
    private static final long serialVersionUID = 1L;

    protected TableExtension getExtension ()
    {
        final TableExtenderTag parent = (TableExtenderTag)findAncestorWithClass ( this, TableExtenderTag.class );
        if ( parent == null )
        {
            return null;
        }

        return parent.getExtension ();
    }

}
