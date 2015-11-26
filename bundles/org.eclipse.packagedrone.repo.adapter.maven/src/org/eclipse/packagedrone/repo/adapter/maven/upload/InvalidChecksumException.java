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

public class InvalidChecksumException extends ChecksumValidationException
{
    private static final long serialVersionUID = 1L;

    private final Coordinates coordinates;

    private final String expectedValue;

    private final String actualValue;

    public InvalidChecksumException ( final Coordinates coordinates, final String expectedValue, final String actualValue )
    {
        super ( String.format ( "Invalid checksum: {} - expected: {}, actual: {}", coordinates, expectedValue, actualValue ) );
        this.coordinates = coordinates;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }

    public Coordinates getCoordinates ()
    {
        return this.coordinates;
    }

    public String getExpectedValue ()
    {
        return this.expectedValue;
    }

    public String getActualValue ()
    {
        return this.actualValue;
    }
}
