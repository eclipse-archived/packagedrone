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
package org.eclipse.packagedrone.repo.importer.job;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.packagedrone.job.ErrorInformation;

public class ImporterResult
{
    private String channelId;

    public static class Entry
    {
        private final String id;

        private final String name;

        private final long size;

        private final ErrorInformation error;

        private final List<Entry> children = new LinkedList<> ();

        /**
         * Successful entry
         */
        public Entry ( final String id, final String name, final long size )
        {
            this.id = id;
            this.name = name;
            this.size = size;
            this.error = null;
        }

        /**
         * Failed entry
         */
        public Entry ( final String name, final ErrorInformation error )
        {
            this.id = null;
            this.name = name;
            this.size = -1;
            this.error = error;
        }

        /**
         * Skipped entry
         */
        public Entry ( final String name )
        {
            this.id = null;
            this.name = name;
            this.size = -1;
            this.error = null;
        }

        public String getId ()
        {
            return this.id;
        }

        public String getName ()
        {
            return this.name;
        }

        public long getSize ()
        {
            return this.size;
        }

        public ErrorInformation getError ()
        {
            return this.error;
        }

        public boolean isSuccess ()
        {
            return this.error == null && this.id != null;
        }

        public boolean isSkipped ()
        {
            return this.id == null && this.error == null;
        }

        public boolean isError ()
        {
            return this.error != null;
        }

        public List<Entry> getChildren ()
        {
            return this.children;
        }
    }

    private List<Entry> entries = new LinkedList<> ();

    private long totalBytes;

    public void setTotalBytes ( final long totalBytes )
    {
        this.totalBytes = totalBytes;
    }

    public long getTotalBytes ()
    {
        return this.totalBytes;
    }

    public void setChannelId ( final String channelId )
    {
        this.channelId = channelId;
    }

    public String getChannelId ()
    {
        return this.channelId;
    }

    public List<Entry> getEntries ()
    {
        return this.entries;
    }

    public void setEntries ( final List<Entry> entries )
    {
        this.entries = entries;
    }
}
