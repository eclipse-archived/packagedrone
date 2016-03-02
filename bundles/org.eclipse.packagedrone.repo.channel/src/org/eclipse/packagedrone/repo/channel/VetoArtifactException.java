/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel;

import java.util.Optional;

public class VetoArtifactException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final String artifactName;

    private final VetoPolicy policy;

    private final Optional<String> vetoMessage;

    public VetoArtifactException ( final String artifactName, final String phase, final VetoPolicy vetoPolicy, final String vetoMessage )
    {
        super ( String.format ( "Veto[%s] %s artifacts - name: %s, message: %s", vetoPolicy, phase, artifactName, vetoMessage ) );
        this.artifactName = artifactName;
        this.policy = vetoPolicy;
        this.vetoMessage = Optional.ofNullable ( vetoMessage );
    }

    public String getArtifactName ()
    {
        return this.artifactName;
    }

    public VetoPolicy getPolicy ()
    {
        return this.policy;
    }

    public Optional<String> getVetoMessage ()
    {
        return this.vetoMessage;
    }
}
