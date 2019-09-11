/*******************************************************************************
 * Copyright (c) 2019 Trident Systems, Inc.
 * This software was developed with U.S government funding in support of the above
 * contract.  Trident grants unlimited rights to modify, distribute and incorporate
 * our contributions to Eclipse Package Drone bound by the overall restrictions from
 * the parent Eclipse Public License v1.0 available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Walker Funk - Trident Systems Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.rpm.internal;

import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.adapter.rpm.Constants;
import org.eclipse.packagedrone.repo.adapter.rpm.internal.RpmSignerVirtualizer;

public class RpmSignerAspectFactory implements ChannelAspectFactory
{

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspect () {

            @Override
            public String getId ()
            {
                return Constants.RPM_SIGN_ASPECT_ID;
            }

            @Override
            public Virtualizer getArtifactVirtualizer ()
            {
                return new RpmSignerVirtualizer ();
            }
        };
    }

}
