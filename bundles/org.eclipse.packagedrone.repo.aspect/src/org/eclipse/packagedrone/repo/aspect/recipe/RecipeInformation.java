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
package org.eclipse.packagedrone.repo.aspect.recipe;

/**
 * Information about a {@link Recipe}
 */
public class RecipeInformation
{
    private final String id;

    private final String label;

    private final String description;

    public RecipeInformation ( final String id, final String label, final String description )
    {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public String getDescription ()
    {
        return this.description;
    }
}
