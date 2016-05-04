/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;

public interface ModifiableChannel extends ReadableChannel, AspectableChannel
{
    /**
     * Get access to more channel operations
     */
    @Override
    public ModifyContext getContext ();

    public default void applyMetaData ( final Map<MetaKey, String> changes )
    {
        getContext ().applyMetaData ( changes );
    }

    /**
     * Lock the channel for further modifications
     */
    public default void lock ()
    {
        getContext ().lock ();
    }

    /**
     * Unlock the channel
     * <p>
     * Reverses the effects of {@link #lock()}
     * </p>
     */
    public default void unlock ()
    {
        getContext ().unlock ();
    }

    @Override
    public default void removeAspects ( final String... aspectIds )
    {
        getContext ().removeAspects ( new HashSet<> ( Arrays.asList ( aspectIds ) ) );
    }

    @Override
    default void refreshAspects ( final String... aspectIds )
    {
        getContext ().refreshAspects ( new HashSet<> ( Arrays.asList ( aspectIds ) ) );
    }
}
