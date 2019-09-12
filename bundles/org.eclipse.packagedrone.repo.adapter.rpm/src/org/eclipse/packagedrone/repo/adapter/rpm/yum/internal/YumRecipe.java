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
package org.eclipse.packagedrone.repo.adapter.rpm.yum.internal;

import org.eclipse.packagedrone.repo.aspect.recipe.Recipe;
import org.eclipse.packagedrone.repo.channel.AspectableChannel;
import org.eclipse.packagedrone.web.LinkTarget;

public class YumRecipe implements Recipe
{
    @Override
    public LinkTarget setup ( final String channelId, final AspectableChannel channel )
    {
        channel.addAspects ( true, "rpm", "yum", "rpm.signer" );
        return null;
    }
}
