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
package org.eclipse.packagedrone.utils.rpm.yum;

public enum ChecksumType
{
    SHA1 ( "sha" ),
    SHA256 ( "sha256" );

    private String id;

    private ChecksumType ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }
}
