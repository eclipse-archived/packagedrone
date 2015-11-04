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
package org.eclipse.packagedrone.repo.channel.apm;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.packagedrone.repo.Severity;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;

public class ValidationMessageModel
{
    private String aspectId;

    private Severity severity;

    private String message;

    private Set<String> artifactIds;

    public void setAspectId ( final String aspectId )
    {
        this.aspectId = aspectId;
    }

    public String getAspectId ()
    {
        return this.aspectId;
    }

    public void setSeverity ( final Severity severity )
    {
        this.severity = severity;
    }

    public Severity getSeverity ()
    {
        return this.severity;
    }

    public void setMessage ( final String message )
    {
        this.message = message;
    }

    public String getMessage ()
    {
        return this.message;
    }

    public void setArtifactIds ( final Set<String> artifactIds )
    {
        this.artifactIds = artifactIds;
    }

    public Set<String> getArtifactIds ()
    {
        return this.artifactIds;
    }

    public static ValidationMessage toMessage ( final ValidationMessageModel model )
    {
        return new ValidationMessage ( model.getAspectId (), model.getSeverity (), model.getMessage (), model.getArtifactIds () );
    }

    public static ValidationMessageModel fromMessage ( final ValidationMessage msg )
    {
        final ValidationMessageModel result = new ValidationMessageModel ();

        result.setSeverity ( msg.getSeverity () );
        result.setAspectId ( msg.getAspectId () );
        result.setMessage ( msg.getMessage () );
        result.setArtifactIds ( new HashSet<> ( msg.getArtifactIds () ) );

        return result;
    }
}
