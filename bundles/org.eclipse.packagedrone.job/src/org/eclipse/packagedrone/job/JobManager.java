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
package org.eclipse.packagedrone.job;

import java.util.Collection;

public interface JobManager
{
    public JobHandle startJob ( JobRequest job );

    public default JobHandle startJob ( final String factoryId, final String data )
    {
        return startJob ( new JobRequest ( factoryId, data ) );
    }

    @Deprecated
    public JobHandle startJob ( String factoryId, Object data );

    public Collection<? extends JobHandle> getActiveJobs ();

    public JobHandle getJob ( String id );

    public JobFactoryDescriptor getFactory ( String factoryId );
}
