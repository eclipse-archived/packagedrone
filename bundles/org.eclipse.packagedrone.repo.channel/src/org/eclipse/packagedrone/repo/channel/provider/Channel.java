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

import org.eclipse.packagedrone.repo.channel.IdTransformer;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperationVoid;

public interface Channel
{
    public String getId ();

    public <T> T accessCall ( ChannelOperation<T, AccessContext> operation, IdTransformer idTransformer );

    public <T> T modifyCall ( ChannelOperation<T, ModifyContext> operation, IdTransformer idTransformer );

    public default void accessRun ( final ChannelOperationVoid<AccessContext> operation, final IdTransformer idTransformer )
    {
        accessCall ( channel -> {
            operation.process ( channel );
            return null;
        } , idTransformer );
    }

    public default void modifyRun ( final ChannelOperationVoid<ModifyContext> operation, final IdTransformer idTransformer )
    {
        modifyCall ( channel -> {
            operation.process ( channel );
            return null;
        } , idTransformer );
    }

    public void delete ();
}
