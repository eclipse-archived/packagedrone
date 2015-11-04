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
package org.eclipse.packagedrone.repo.adapter.deb.servlet.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.adapter.deb.servlet.Helper;

public class ContentHandler implements Handler
{
    private final URL resource;

    private final Map<String, Object> model;

    private final String title;

    public ContentHandler ( final URL resource, final String title, final Map<String, Object> model )
    {
        this.resource = resource;
        this.model = model;
        this.title = title;
    }

    @Override
    public void process ( final OutputStream stream ) throws IOException
    {
    }

    @Override
    public void process ( final HttpServletResponse response ) throws IOException
    {
        Helper.render ( response, this.resource, this.title, this.model );
    }

}
