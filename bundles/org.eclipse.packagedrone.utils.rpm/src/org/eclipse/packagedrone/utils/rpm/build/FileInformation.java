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
package org.eclipse.packagedrone.utils.rpm.build;

import java.time.Instant;

public class FileInformation
{
    private Instant timestamp = Instant.now ();

    private String user = BuilderContext.DEFAULT_USER;

    private String group = BuilderContext.DEFAULT_GROUP;

    private boolean configuration = false;

    private short mode = 0644;

    public void setTimestamp ( final Instant timestamp )
    {
        this.timestamp = timestamp;
    }

    public Instant getTimestamp ()
    {
        return timestamp;
    }

    public void setConfiguration ( final boolean configuration )
    {
        this.configuration = configuration;
    }

    public boolean isConfiguration ()
    {
        return configuration;
    }

    public void setUser ( final String user )
    {
        this.user = user;
    }

    public String getUser ()
    {
        return user;
    }

    public void setGroup ( final String group )
    {
        this.group = group;
    }

    public String getGroup ()
    {
        return group;
    }

    public void setMode ( final short mode )
    {
        this.mode = mode;
    }

    public short getMode ()
    {
        return mode;
    }
}