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
package org.eclipse.packagedrone.repo.importer.web;

import org.eclipse.packagedrone.utils.converter.JSON;

import com.google.gson.GsonBuilder;

@JSON
public class ImportRequest
{
    private final String importerId;

    private final String configuration;

    public ImportRequest ( final String importerId, final String configuration )
    {
        this.importerId = importerId;
        this.configuration = configuration;
    }

    public String getImporterId ()
    {
        return this.importerId;
    }

    public String getConfiguration ()
    {
        return this.configuration;
    }

    public static String toJson ( final String importerId, final String configuration )
    {
        return new GsonBuilder ().create ().toJson ( new ImportRequest ( importerId, configuration ) );
    }
}
