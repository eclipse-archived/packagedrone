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
package org.eclipse.packagedrone.repo.event;

import java.util.Collections;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;

public abstract class ArtifactEvent
{
    private final Map<MetaKey, String> metaData;

    private final String artifactId;

    public ArtifactEvent ( final String artifactId, final Map<MetaKey, String> metaData )
    {
        this.artifactId = artifactId;
        this.metaData = Collections.unmodifiableMap ( metaData );
    }

    public String getArtifactId ()
    {
        return this.artifactId;
    }

    public Map<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[%s - id: %s]", getClass ().getSimpleName (), this.artifactId );
    }
}
