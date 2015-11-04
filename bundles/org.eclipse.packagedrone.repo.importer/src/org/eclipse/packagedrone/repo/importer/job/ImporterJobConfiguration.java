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
package org.eclipse.packagedrone.repo.importer.job;

import org.eclipse.packagedrone.repo.importer.web.ImportDescriptor;

public class ImporterJobConfiguration
{
    private ImportDescriptor descriptor;

    private String importerId;

    private String configuration;

    public ImporterJobConfiguration ()
    {
    }

    public ImporterJobConfiguration ( final ImportDescriptor descriptor, final String importerId, final String configuration )
    {
        this.descriptor = descriptor;
        this.importerId = importerId;
        this.configuration = configuration;
    }

    public ImportDescriptor getDescriptor ()
    {
        return this.descriptor;
    }

    public void setDescriptor ( final ImportDescriptor descriptor )
    {
        this.descriptor = descriptor;
    }

    public String getImporterId ()
    {
        return this.importerId;
    }

    public void setImporterId ( final String importerId )
    {
        this.importerId = importerId;
    }

    public String getConfiguration ()
    {
        return this.configuration;
    }

    public void setConfiguration ( final String configuration )
    {
        this.configuration = configuration;
    }

}
