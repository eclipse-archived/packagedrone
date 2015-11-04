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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.job.JobHandle;
import org.eclipse.packagedrone.job.apm.JobHandleImpl;

public class JobModel
{
    public Map<String, JobHandle> jobs;

    public JobModel ( final Collection<JobInstanceEntity> jobs )
    {
        this.jobs = new HashMap<> ( jobs.size () );
        for ( final JobInstanceEntity ji : jobs )
        {
            this.jobs.put ( ji.getId (), new JobHandleImpl ( ji ) );
        }
    }

    public Map<String, JobHandle> getJobs ()
    {
        return this.jobs;
    }
}
