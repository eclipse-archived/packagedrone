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
package org.eclipse.packagedrone.repo.importer.http;

import javax.validation.constraints.NotNull;

import org.eclipse.packagedrone.utils.converter.JSON;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

@JSON
public class Configuration
{
    @URL
    @NotEmpty
    @NotNull
    private String url;

    public void setUrl ( final String url )
    {
        this.url = url;
    }

    public String getUrl ()
    {
        return this.url;
    }

    private String alternateName;

    public void setAlternateName ( final String alternateName )
    {
        this.alternateName = alternateName;
    }

    public String getAlternateName ()
    {
        return this.alternateName;
    }
}
