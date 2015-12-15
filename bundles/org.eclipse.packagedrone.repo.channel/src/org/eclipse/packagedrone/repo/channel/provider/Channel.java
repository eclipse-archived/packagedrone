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
package org.eclipse.packagedrone.repo.channel.provider;

import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperationVoid;

public interface Channel
{
    public <T> T accessCall ( ChannelOperation<T, AccessContext> operation );

    public <T> T modifyCall ( ChannelOperation<T, ModifyContext> operation );

    public default void accessRun ( final ChannelOperationVoid<AccessContext> operation )
    {
        accessCall ( channel -> {
            operation.process ( channel );
            return null;
        } );
    }

    public default void modifyRun ( final ChannelOperationVoid<ModifyContext> operation )
    {
        modifyCall ( channel -> {
            operation.process ( channel );
            return null;
        } );
    }

    /**
     * Delete the channel on the persistent storage
     * <p>
     * This method implicitly calls {@link #dispose()}
     * </p>
     */
    public void delete ();

    /**
     * Close all resources associated with the channel
     * <p>
     * Further calls to access and modify methods will fail
     * </p>
     */
    public void dispose ();
}
