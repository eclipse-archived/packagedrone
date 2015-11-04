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

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.job.State;

/**
 * The write model of the job storage
 */
public class JobWriteModel
{
    private static final Duration JOB_FORGET_DURATION = Duration.ofHours ( 8 );

    private final Map<String, JobInstanceEntity> jobs;

    private final Map<String, JobInstanceEntity> cloneMap = new HashMap<> ();

    public JobWriteModel ()
    {
        this.jobs = new HashMap<> ();
    }

    public JobWriteModel ( final Map<String, JobInstanceEntity> jobs )
    {
        this.jobs = jobs;
    }

    /**
     * Get a job instance for updating it.
     * <p>
     * The fetched job will automatically be cloned and persisted.
     * </p>
     *
     * @param id
     *            the id of the job
     * @return the job instance entity or <code>null</code> if none could be
     *         found
     */
    public JobInstanceEntity getJobForUpdate ( final String id )
    {
        JobInstanceEntity job = this.cloneMap.get ( id );

        if ( job != null )
        {
            // this already is the cloned job
            return job;
        }

        // fetch the job
        job = this.jobs.get ( id );
        if ( job == null )
        {
            return null;
        }

        // make the clone
        final JobInstanceEntity clonedJob = new JobInstanceEntity ( job );

        this.cloneMap.put ( id, clonedJob );

        return clonedJob;
    }

    public Map<String, JobInstanceEntity> makeJobMap ()
    {
        final Map<String, JobInstanceEntity> result = new HashMap<> ( this.jobs );

        final Instant timedOut = Instant.now ().minus ( JOB_FORGET_DURATION );

        cloneActiveJobs ( this.jobs.values (), result, timedOut );
        cloneActiveJobs ( this.cloneMap.values (), result, timedOut );

        return result;
    }

    protected static void cloneActiveJobs ( final Collection<JobInstanceEntity> jobs, final Map<String, JobInstanceEntity> result, final Instant timedOut )
    {
        jobs.stream ().filter ( job -> job.getState () != State.COMPLETE || job.getCreation ().isAfter ( timedOut ) ).forEach ( job -> result.put ( job.getId (), job ) );
    }

    public void addJob ( JobInstanceEntity ji )
    {
        ji = new JobInstanceEntity ( ji );

        if ( ji.getId () == null )
        {
            throw new NullPointerException ( "Job ID must not be null" );
        }

        this.cloneMap.put ( ji.getId (), ji );
    }

}
