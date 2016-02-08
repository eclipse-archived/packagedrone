/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel;

import java.nio.file.Path;

import org.eclipse.packagedrone.repo.channel.search.ArtifactLocator;

public interface PreAddContext
{
    public Path getFile ();

    public String getName ();

    public default void vetoAdd ()
    {
        vetoAdd ( VetoPolicy.REJECT );
    }

    public void vetoAdd ( VetoPolicy policy );

    /**
     * A flag if this is an external or internal add operation
     */
    public boolean isExternal ();

    /**
     * Get an artifact locator for the current channel state
     * <p>
     * The artifact locator will work on the current channel state, which may or
     * may not get committed. The artifact being processes by the request is not
     * already part of this state.
     * </p>
     *
     * @return the artifact locator, never returns {@code null}
     */
    public ArtifactLocator getArtifactLocator ();
}
