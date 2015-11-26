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

import java.util.Map;

/**
 * A job information object
 * <p>
 * The object represents the state of job when it was returned by
 * {@link JobManager#getJob(String)}
 * </p>
 */
public interface JobHandle
{
    public String getId ();

    public State getState ();

    public JobRequest getRequest ();

    public String getLabel ();

    public ErrorInformation getError ();

    public String getResult ();

    /**
     * Return the job properties
     *
     * @return the job properties, may be an empty map, but never {@code null}
     */
    public Map<String, String> getProperties ();

    public default boolean isComplete ()
    {
        return getState () == State.COMPLETE;
    }

    public default boolean isFailed ()
    {
        return isComplete () && getError () != null;
    }
}
