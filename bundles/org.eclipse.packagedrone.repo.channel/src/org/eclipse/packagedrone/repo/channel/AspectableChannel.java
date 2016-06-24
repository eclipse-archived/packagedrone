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

import java.util.Objects;
import java.util.Set;

public interface AspectableChannel
{
    public void addAspects ( boolean withDependencies, String... aspectIds );

    public default void addAspects ( final boolean withDependencies, final Set<String> aspectIds )
    {
        Objects.requireNonNull ( aspectIds );
        addAspects ( withDependencies, aspectIds.toArray ( new String[aspectIds.size ()] ) );
    }

    public void removeAspects ( String... aspectIds );

    public default void removeAspects ( final Set<String> aspectIds )
    {
        Objects.requireNonNull ( aspectIds );
        removeAspects ( aspectIds.toArray ( new String[aspectIds.size ()] ) );
    }

    public void refreshAspects ( String... aspectIds );

    public default void refreshAspects ( final Set<String> aspectIds )
    {
        Objects.requireNonNull ( aspectIds );
        refreshAspects ( aspectIds.toArray ( new String[aspectIds.size ()] ) );
    }
}
