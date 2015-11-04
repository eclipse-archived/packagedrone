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
package org.eclipse.packagedrone.repo.aspect.common.p2;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.common.p2.internal.P2Unzipper;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;

public class P2UnzipAspectFactory implements ChannelAspectFactory
{
    public static final String ID = "p2.unzip";

    public static final MetaKey MK_FULL_NAME = new MetaKey ( ID, "fullName" );

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspect () {

            @Override
            public String getId ()
            {
                return ID;
            }

            @Override
            public Virtualizer getArtifactVirtualizer ()
            {
                return new P2Unzipper ();
            }
        };
    }

}
