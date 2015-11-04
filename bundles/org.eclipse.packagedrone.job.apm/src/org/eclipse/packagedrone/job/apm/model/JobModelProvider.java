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
package org.eclipse.packagedrone.job.apm.model;

import org.eclipse.packagedrone.storage.apm.AbstractSimpleStorageModelProvider;
import org.eclipse.packagedrone.storage.apm.StorageContext;

public class JobModelProvider extends AbstractSimpleStorageModelProvider<JobModel, JobWriteModel>
{
    public JobModelProvider ()
    {
        super ( JobModel.class, JobWriteModel.class );
    }

    @Override
    public JobWriteModel cloneWriteModel ( final JobWriteModel writeModel )
    {
        return new JobWriteModel ( writeModel.makeJobMap () );
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final JobWriteModel writeModel ) throws Exception
    {
        // right now we don't persist jobs
    }

    @Override
    protected JobModel makeViewModelTyped ( final JobWriteModel writeModel )
    {
        return new JobModel ( writeModel.makeJobMap ().values () );
    }

    @Override
    protected JobWriteModel loadWriteModel ( final StorageContext context ) throws Exception
    {
        return new JobWriteModel ();
    }

}
