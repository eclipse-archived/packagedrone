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

import java.util.Date;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;

public class CacheEntryModel
{
    private String name;

    private long size;

    private String mimeType;

    private Date timestamp;

    public CacheEntryModel ()
    {
    }

    public CacheEntryModel ( final CacheEntryModel other )
    {
        this.name = other.name;
        this.size = other.size;
        this.mimeType = other.mimeType;
        this.timestamp = new Date ( other.timestamp.getTime () );
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public long getSize ()
    {
        return this.size;
    }

    public void setSize ( final long size )
    {
        this.size = size;
    }

    public String getMimeType ()
    {
        return this.mimeType;
    }

    public void setMimeType ( final String mimeType )
    {
        this.mimeType = mimeType;
    }

    public Date getTimestamp ()
    {
        return this.timestamp;
    }

    public void setTimestamp ( final Date timestamp )
    {
        this.timestamp = timestamp;
    }

    public static CacheEntryInformation toEntry ( final MetaKey key, final CacheEntryModel model )
    {
        return new CacheEntryInformation ( key, model.name, model.size, model.mimeType, model.timestamp.toInstant () );
    }

    public static CacheEntryModel fromInformation ( final CacheEntryInformation entry )
    {
        final CacheEntryModel result = new CacheEntryModel ();

        result.setSize ( entry.getSize () );
        result.setName ( entry.getName () );
        result.setMimeType ( entry.getMimeType () );
        result.setTimestamp ( entry.getTimestampAsDate () );

        return result;
    }
}
