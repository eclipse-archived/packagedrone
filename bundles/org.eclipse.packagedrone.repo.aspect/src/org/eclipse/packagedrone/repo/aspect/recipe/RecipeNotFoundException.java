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

public class RecipeNotFoundException extends Exception
{
    private static final long serialVersionUID = 1L;

    public RecipeNotFoundException ( final String recipeId )
    {
        super ( String.format ( "Recipe '%s' could not be found", recipeId ) );
    }
}
