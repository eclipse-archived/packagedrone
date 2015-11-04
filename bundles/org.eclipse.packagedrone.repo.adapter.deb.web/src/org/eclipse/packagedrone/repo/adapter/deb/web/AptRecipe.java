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
package org.eclipse.packagedrone.repo.adapter.deb.web;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.aspect.recipe.Recipe;
import org.eclipse.packagedrone.repo.channel.AspectableChannel;
import org.eclipse.packagedrone.web.LinkTarget;

public class AptRecipe implements Recipe
{
    @Override
    public LinkTarget setup ( final String channelId, final AspectableChannel channel )
    {
        channel.addAspects ( true, "apt", "deb" );

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "channelId", channelId );
        return LinkTarget.createFromController ( ConfigController.class, "edit" ).expand ( model );
    }
}
