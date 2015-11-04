/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.eclipse.packagedrone.utils.Strings;

public class BytesTag extends SimpleTagSupport
{

    public BytesTag ()
    {

    }

    private Long amount;

    public void setAmount ( final Long amount )
    {
        this.amount = amount;
    }

    @Override
    public void doTag () throws JspException, IOException
    {
        if ( this.amount != null )
        {
            getJspContext ().getOut ().write ( bytes ( this.amount ) );
        }
    }

    public String bytes ( final long amount )
    {
        return Strings.bytes ( amount );
    }
}
