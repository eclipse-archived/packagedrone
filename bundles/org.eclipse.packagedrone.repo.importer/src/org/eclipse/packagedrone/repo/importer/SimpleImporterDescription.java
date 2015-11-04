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
package org.eclipse.packagedrone.repo.importer;

import org.eclipse.packagedrone.web.LinkTarget;

public class SimpleImporterDescription implements ImporterDescription
{
    private String id;

    private String label;

    private String description;

    private LinkTarget startTarget;

    public SimpleImporterDescription ()
    {
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    @Override
    public String getLabel ()
    {
        return this.label;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
    }

    @Override
    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    @Override
    public LinkTarget getConfigurationTarget ()
    {
        return this.startTarget;
    }

    public void setStartTarget ( final LinkTarget startTarget )
    {
        this.startTarget = startTarget;
    }
}
