/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.listener;

import org.eclipse.packagedrone.repo.channel.PreAddContext;

public interface ChannelListener
{
    /**
     * Process a request to add an artifact
     * <p>
     * In general it is possible to check an incoming (not stored yet) artifact
     * and veto its creation.
     * </p>
     *
     * @param context
     *            the context information
     * @throws Exception
     *             if anything goes wrong
     */
    public default void artifactPreAdd ( final PreAddContext context ) throws Exception
    {
    }

    public default void artifactAdded ( final PostAddContext context ) throws Exception
    {
    }
}
