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
package org.eclipse.packagedrone.repo.api.upload;

public class ArtifactInformation
{
    private String id;

    private String parentId;

    private String name;

    private long size;

    private long errors;

    private long warnings;

    public String getId ()
    {
        return this.id;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getParentId ()
    {
        return this.parentId;
    }

    public void setParentId ( final String parentId )
    {
        this.parentId = parentId;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public long getSize ()
    {
        return this.size;
    }

    public void setSize ( final long size )
    {
        this.size = size;
    }

    public long getErrors ()
    {
        return this.errors;
    }

    public void setErrors ( final long errors )
    {
        this.errors = errors;
    }

    public long getWarnings ()
    {
        return this.warnings;
    }

    public void setWarnings ( final long warnings )
    {
        this.warnings = warnings;
    }
}
