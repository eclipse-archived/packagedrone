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

import java.time.Instant;

import org.eclipse.packagedrone.job.ErrorInformation;
import org.eclipse.packagedrone.job.State;

public class JobInstanceEntity
{
    private String id;

    private String factoryId;

    private final Instant creation;

    private String data;

    private String result;

    private State state;

    private ErrorInformation errorInformation;

    private String label;

    private String currentWorkLabel;

    private Double percentComplete;

    public JobInstanceEntity ()
    {
        this.creation = Instant.now ();
    }

    public JobInstanceEntity ( final JobInstanceEntity other )
    {
        this.id = other.id;
        this.factoryId = other.factoryId;
        this.creation = other.creation;
        this.data = other.data;
        this.result = other.result;
        this.state = other.state;
        this.errorInformation = other.errorInformation;
        this.label = other.label;
        this.currentWorkLabel = other.currentWorkLabel;
        this.percentComplete = other.percentComplete;
    }

    public Instant getCreation ()
    {
        return this.creation;
    }

    public void setPercentComplete ( final Double percentComplete )
    {
        this.percentComplete = percentComplete;
    }

    public Double getPercentComplete ()
    {
        return this.percentComplete;
    }

    public void setCurrentWorkLabel ( final String currentWorkLabel )
    {
        this.currentWorkLabel = currentWorkLabel;
    }

    public String getCurrentWorkLabel ()
    {
        return this.currentWorkLabel;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public void setResult ( final String result )
    {
        this.result = result;
    }

    public String getResult ()
    {
        return this.result;
    }

    public void setFactoryId ( final String factoryId )
    {
        this.factoryId = factoryId;
    }

    public String getFactoryId ()
    {
        return this.factoryId;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setData ( final String data )
    {
        this.data = data;
    }

    public String getData ()
    {
        return this.data;
    }

    public void setState ( final State state )
    {
        this.state = state;
    }

    public State getState ()
    {
        return this.state;
    }

    public ErrorInformation getErrorInformation ()
    {
        return this.errorInformation;
    }

    public void setErrorInformation ( final ErrorInformation errorInformation )
    {
        this.errorInformation = errorInformation;
    }
}
