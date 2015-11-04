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
package org.eclipse.packagedrone.sec.web.tags;

import java.security.Principal;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.eclipse.packagedrone.sec.UserInformationPrincipal;

public class UserInformationTag extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private String var = "userDetails";

    public void setVar ( final String var )
    {
        this.var = var != null ? var : "userDetails";
    }

    @Override
    public int doStartTag () throws JspException
    {
        final Principal p = getPrincipal ();

        if ( p instanceof UserInformationPrincipal )
        {
            this.pageContext.setAttribute ( this.var, ( (UserInformationPrincipal)p ).getUserInformation () );
        }

        return SKIP_BODY;
    }

    protected Principal getPrincipal ()
    {
        final ServletRequest request = this.pageContext.getRequest ();
        if ( request instanceof HttpServletRequest )
        {
            return ( (HttpServletRequest)request ).getUserPrincipal ();

        }
        return null;
    }
}
