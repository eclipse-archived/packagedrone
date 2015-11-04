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
package org.eclipse.packagedrone.job.apm;

import org.eclipse.packagedrone.job.ErrorInformation;
import org.eclipse.packagedrone.job.JobHandle;
import org.eclipse.packagedrone.job.JobRequest;
import org.eclipse.packagedrone.job.State;
import org.eclipse.packagedrone.job.apm.model.JobInstanceEntity;

public class JobHandleImpl implements JobHandle
{
    private final String id;

    private final State state;

    private final ErrorInformation errorInformation;

    private final JobRequest request;

    private final String label;

    private final String result;

    private final String currentWorkLabel;

    private final Double percentComplete;

    public JobHandleImpl ( final JobInstanceEntity ji )
    {
        this.id = ji.getId ();
        this.state = ji.getState ();
        this.errorInformation = ji.getErrorInformation ();

        this.currentWorkLabel = ji.getCurrentWorkLabel ();
        this.percentComplete = ji.getPercentComplete ();

        this.request = new JobRequest ();
        this.request.setFactoryId ( ji.getFactoryId () );
        this.request.setData ( ji.getData () );

        this.label = ji.getLabel ();
        this.result = ji.getResult ();
    }

    @Override
    public String getResult ()
    {
        return this.result;
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public State getState ()
    {
        return this.state;
    }

    @Override
    public ErrorInformation getError ()
    {
        return this.errorInformation;
    }

    @Override
    public JobRequest getRequest ()
    {
        return this.request;
    }

    @Override
    public String getLabel ()
    {
        return this.label;
    }

    public String getCurrentWorkLabel ()
    {
        return this.currentWorkLabel;
    }

    public double getPercentComplete ()
    {
        return this.percentComplete == null ? 0.0 : this.percentComplete;
    }

    @Override
    public boolean isComplete ()
    {
        return getState () == State.COMPLETE;
    }
}
