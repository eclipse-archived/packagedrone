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
package org.eclipse.packagedrone.repo.channel.deploy;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;

public class DeployKey
{
    public final static Comparator<DeployKey> NAME_COMPARATOR = nullsFirst ( comparing ( DeployKey::getName ) );

    private final DeployGroup group;

    private final String id;

    private final String name;

    private final String key;

    private final Instant creationTimestamp;

    public DeployKey ( final DeployGroup group, final String id, final String name, final String key, final Instant creationTimestamp )
    {
        this.id = id;
        this.group = group;
        this.name = name;
        this.key = key;
        this.creationTimestamp = creationTimestamp;
    }

    public DeployGroup getGroup ()
    {
        return this.group;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getKey ()
    {
        return this.key;
    }

    public String getName ()
    {
        return this.name;
    }

    public Instant getCreationTimestamp ()
    {
        return this.creationTimestamp;
    }

    public Date getCreationDate ()
    {
        return Date.from ( this.creationTimestamp );
    }

}
