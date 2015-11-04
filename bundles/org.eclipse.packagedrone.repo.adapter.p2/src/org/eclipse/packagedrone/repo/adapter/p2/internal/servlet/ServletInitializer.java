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
package org.eclipse.packagedrone.repo.adapter.p2.internal.servlet;

import javax.servlet.Servlet;

import org.eclipse.packagedrone.web.dispatcher.JspServletInitializer;
import org.osgi.framework.FrameworkUtil;

public class ServletInitializer extends JspServletInitializer
{
    public ServletInitializer ()
    {
        super ( "/p2", FrameworkUtil.getBundle ( ServletInitializer.class ) );
    }

    @Override
    protected Servlet createServlet ()
    {
        return new P2Servlet ();
    }
}
