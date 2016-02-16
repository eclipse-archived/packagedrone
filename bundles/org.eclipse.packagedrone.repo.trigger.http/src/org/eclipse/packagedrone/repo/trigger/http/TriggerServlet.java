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
package org.eclipse.packagedrone.repo.trigger.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TriggerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final Runnable triggerRunner;

    public TriggerServlet ( final Runnable triggerRunner )
    {
        this.triggerRunner = triggerRunner;
    }

    @Override
    protected void doPost ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        this.triggerRunner.run ();
    }
}
