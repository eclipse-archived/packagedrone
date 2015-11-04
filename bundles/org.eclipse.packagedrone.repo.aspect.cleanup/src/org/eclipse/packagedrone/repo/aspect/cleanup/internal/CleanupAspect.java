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
package org.eclipse.packagedrone.repo.aspect.cleanup.internal;

import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.listener.ChannelListener;

public class CleanupAspect implements ChannelAspect
{
    public static final String ID = "cleanup";

    @Override
    public String getId ()
    {
        return ID;
    }

    @Override
    public ChannelListener getChannelListener ()
    {
        return new CleanupListener ();
    }
}
