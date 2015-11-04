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
package org.eclipse.packagedrone.repo.generator.p2;

import javax.validation.constraints.Pattern;

import org.eclipse.packagedrone.repo.MetaKeyBinding;

public class CategoryData
{
    @Pattern ( regexp = "[a-z0-9]+(\\.[a-z0-9]+)*", message = "Must be a valid category ID" )
    @MetaKeyBinding ( namespace = CategoryGenerator.ID, key = "id" )
    private String id;

    @MetaKeyBinding ( namespace = CategoryGenerator.ID, key = "name" )
    private String name;

    @MetaKeyBinding ( namespace = CategoryGenerator.ID, key = "description" )
    private String description;

    public String getId ()
    {
        return this.id;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

}
