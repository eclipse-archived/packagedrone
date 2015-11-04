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
package org.eclipse.packagedrone.repo.channel;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.packagedrone.repo.Severity;

public class ValidationMessage
{
    private final Severity severity;

    private final String message;

    private final String aspectId;

    private final Set<String> artifactIds;

    public ValidationMessage ( final String aspectId, final Severity severity, final String message, final Set<String> artifactIds )
    {
        this.aspectId = aspectId;
        this.severity = severity;
        this.message = message;
        this.artifactIds = Collections.unmodifiableSet ( new TreeSet<> ( artifactIds ) /* use sorted map */ );
    }

    public String getAspectId ()
    {
        return this.aspectId;
    }

    public Severity getSeverity ()
    {
        return this.severity;
    }

    public String getMessage ()
    {
        return this.message;
    }

    public Set<String> getArtifactIds ()
    {
        return this.artifactIds;
    }
}
