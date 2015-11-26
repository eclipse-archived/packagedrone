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
package org.eclipse.packagedrone.repo.adapter.maven.upload;

public class ArtifactNotFoundException extends ChecksumValidationException
{
    private static final long serialVersionUID = 1L;

    private final Coordinates coordinates;

    public ArtifactNotFoundException ( final Coordinates coordinates )
    {
        super ( String.format ( "Unable to find artifact: %s", coordinates ) );
        this.coordinates = coordinates;
    }

    public Coordinates getCoordinates ()
    {
        return this.coordinates;
    }
}
