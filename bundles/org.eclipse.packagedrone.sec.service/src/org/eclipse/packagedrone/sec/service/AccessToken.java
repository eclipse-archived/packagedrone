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
package org.eclipse.packagedrone.sec.service;

import java.time.Instant;

public class AccessToken
{
    private final String id;

    private final String token;

    private final String description;

    private final Instant creationTimestamp;

    public AccessToken ( final String id, final String token, final String description, final Instant creationTimestamp )
    {
        this.id = id;
        this.token = token;
        this.description = description;
        this.creationTimestamp = creationTimestamp;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getToken ()
    {
        return this.token;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public Instant getCreationTimestamp ()
    {
        return this.creationTimestamp;
    }
}
