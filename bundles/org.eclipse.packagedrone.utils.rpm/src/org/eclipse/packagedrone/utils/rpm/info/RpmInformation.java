/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.info;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RpmInformation
{
    public static class Version
    {
        private String version;

        private String release;

        private String epoch;

        public Version ()
        {
        }

        public Version ( final String version, final String release, final String epoch )
        {
            this.version = version;
            this.release = release;
            this.epoch = epoch;
        }

        public String getVersion ()
        {
            return this.version;
        }

        public void setVersion ( final String version )
        {
            this.version = version;
        }

        public String getRelease ()
        {
            return this.release;
        }

        public void setRelease ( final String release )
        {
            this.release = release;
        }

        public String getEpoch ()
        {
            return this.epoch;
        }

        public void setEpoch ( final String epoch )
        {
            this.epoch = epoch;
        }
    }

    public static class Changelog
    {
        private long timestamp;

        private String author;

        private String text;

        public Changelog ()
        {
        }

        public Changelog ( final long timestamp, final String author, final String text )
        {
            this.timestamp = timestamp;
            this.author = author;
            this.text = text;
        }

        public long getTimestamp ()
        {
            return this.timestamp;
        }

        public void setTimestamp ( final long timestamp )
        {
            this.timestamp = timestamp;
        }

        public String getAuthor ()
        {
            return this.author;
        }

        public void setAuthor ( final String author )
        {
            this.author = author;
        }

        public String getText ()
        {
            return this.text;
        }

        public void setText ( final String text )
        {
            this.text = text;
        }

    }

    public static class Dependency
    {
        private String name;

        private String version;

        private long flags;

        public Dependency ()
        {
        }

        public Dependency ( final String name, final String version, final long flags )
        {
            this.name = name;
            this.version = version;
            this.flags = flags;
        }

        public String getName ()
        {
            return this.name;
        }

        public void setName ( final String name )
        {
            this.name = name;
        }

        public String getVersion ()
        {
            return this.version;
        }

        public void setVersion ( final String version )
        {
            this.version = version;
        }

        public long getFlags ()
        {
            return this.flags;
        }

        public void setFlags ( final long flags )
        {
            this.flags = flags;
        }
    }

    private String name;

    private Version version;

    private String architecture;

    private String license;

    private List<Changelog> changelog = new LinkedList<> ();

    private Set<String> files = new HashSet<> ();

    private Set<String> directories = new HashSet<> ();

    private List<Dependency> provides = new LinkedList<> ();

    private List<Dependency> requires = new LinkedList<> ();

    private List<Dependency> obsoletes = new LinkedList<> ();

    private List<Dependency> conflicts = new LinkedList<> ();

    private String summary;

    private String description;

    private String packager;

    private String vendor;

    private String url;

    private String buildHost;

    private String group;

    private Long installedSize;

    private Long archiveSize;

    private Long buildTimestamp;

    private long headerStart;

    private long headerEnd;

    private String sourcePackage;

    public void setSourcePackage ( final String sourcePackage )
    {
        this.sourcePackage = sourcePackage;
    }

    public String getSourcePackage ()
    {
        return this.sourcePackage;
    }

    public void setGroup ( final String group )
    {
        this.group = group;
    }

    public String getGroup ()
    {
        return this.group;
    }

    public void setLicense ( final String license )
    {
        this.license = license;
    }

    public String getLicense ()
    {
        return this.license;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public Version getVersion ()
    {
        return this.version;
    }

    public void setVersion ( final Version version )
    {
        this.version = version;
    }

    public String getArchitecture ()
    {
        return this.architecture;
    }

    public void setArchitecture ( final String architecture )
    {
        this.architecture = architecture;
    }

    public List<Changelog> getChangelog ()
    {
        return this.changelog;
    }

    public void setChangelog ( final List<Changelog> changelog )
    {
        this.changelog = changelog;
    }

    public Set<String> getFiles ()
    {
        return this.files;
    }

    public void setFiles ( final Set<String> files )
    {
        this.files = files;
    }

    public Set<String> getDirectories ()
    {
        return this.directories;
    }

    public void setDirectories ( final Set<String> directories )
    {
        this.directories = directories;
    }

    public String getSummary ()
    {
        return this.summary;
    }

    public void setSummary ( final String summary )
    {
        this.summary = summary;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public String getPackager ()
    {
        return this.packager;
    }

    public void setPackager ( final String packager )
    {
        this.packager = packager;
    }

    public String getUrl ()
    {
        return this.url;
    }

    public void setUrl ( final String url )
    {
        this.url = url;
    }

    public String getVendor ()
    {
        return this.vendor;
    }

    public void setVendor ( final String vendor )
    {
        this.vendor = vendor;
    }

    public String getBuildHost ()
    {
        return this.buildHost;
    }

    public void setBuildHost ( final String buildHost )
    {
        this.buildHost = buildHost;
    }

    public Long getInstalledSize ()
    {
        return this.installedSize;
    }

    public void setInstalledSize ( final Long installedSize )
    {
        this.installedSize = installedSize;
    }

    public Long getArchiveSize ()
    {
        return this.archiveSize;
    }

    public void setArchiveSize ( final Long archiveSize )
    {
        this.archiveSize = archiveSize;
    }

    public Long getBuildTimestamp ()
    {
        return this.buildTimestamp;
    }

    public void setBuildTimestamp ( final Long buildTimestamp )
    {
        this.buildTimestamp = buildTimestamp;
    }

    public long getHeaderStart ()
    {
        return this.headerStart;
    }

    public void setHeaderStart ( final long headerStart )
    {
        this.headerStart = headerStart;
    }

    public long getHeaderEnd ()
    {
        return this.headerEnd;
    }

    public void setHeaderEnd ( final long headerEnd )
    {
        this.headerEnd = headerEnd;
    }

    public List<Dependency> getProvides ()
    {
        return this.provides;
    }

    public void setProvides ( final List<Dependency> provides )
    {
        this.provides = provides;
    }

    public List<Dependency> getRequires ()
    {
        return this.requires;
    }

    public void setRequires ( final List<Dependency> requires )
    {
        this.requires = requires;
    }

    public List<Dependency> getObsoletes ()
    {
        return this.obsoletes;
    }

    public void setObsoletes ( final List<Dependency> obsoletes )
    {
        this.obsoletes = obsoletes;
    }

    public List<Dependency> getConflicts ()
    {
        return this.conflicts;
    }

    public void setConflicts ( final List<Dependency> conflicts )
    {
        this.conflicts = conflicts;
    }
}
