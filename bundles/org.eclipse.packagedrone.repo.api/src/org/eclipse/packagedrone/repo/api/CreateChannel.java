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
package org.eclipse.packagedrone.repo.api;

import java.util.HashSet;
import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class CreateChannel
{
    private Set<String> names = new HashSet<> ();

    private Set<String> aspects = new HashSet<> ();

    @ApiModelProperty ( required = false, value = "Wether to add aspects with or without required dependencies" )
    private boolean aspectsWithDependencies = true;

    private String description;

    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public Set<String> getNames ()
    {
        return this.names;
    }

    public void setNames ( final Set<String> names )
    {
        this.names = names;
    }

    public Set<String> getAspects ()
    {
        return this.aspects;
    }

    public void setAspects ( final Set<String> aspects )
    {
        this.aspects = aspects;
    }

    public boolean isAspectsWithDependencies ()
    {
        return this.aspectsWithDependencies;
    }

    public void setAspectsWithDependencies ( final boolean aspectsWithDependencies )
    {
        this.aspectsWithDependencies = aspectsWithDependencies;
    }

}
