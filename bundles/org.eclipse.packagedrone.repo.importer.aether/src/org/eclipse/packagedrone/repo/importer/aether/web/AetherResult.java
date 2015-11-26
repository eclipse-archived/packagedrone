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
package org.eclipse.packagedrone.repo.importer.aether.web;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.packagedrone.repo.importer.aether.MavenCoordinates;
import org.eclipse.packagedrone.utils.converter.JSON;

import com.google.gson.GsonBuilder;

@JSON
public class AetherResult
{
    public static class Entry
    {
        private MavenCoordinates coordinates;

        private boolean resolved;

        private String error;

        private boolean requested;

        private boolean optional;

        private Set<String> existingVersions = new TreeSet<> ();

        public void setCoordinates ( final MavenCoordinates coordinates )
        {
            this.coordinates = coordinates;
        }

        public MavenCoordinates getCoordinates ()
        {
            return this.coordinates;
        }

        public void setResolved ( final boolean resolved )
        {
            this.resolved = resolved;
        }

        public boolean isResolved ()
        {
            return this.resolved;
        }

        public void setError ( final String error )
        {
            this.error = error;
        }

        public String getError ()
        {
            return this.error;
        }

        public void setRequested ( final boolean requested )
        {
            this.requested = requested;
        }

        public boolean isRequested ()
        {
            return this.requested;
        }

        public void setOptional ( final boolean optional )
        {
            this.optional = optional;
        }

        public boolean isOptional ()
        {
            return this.optional;
        }

        public void setExistingVersions ( final Set<String> existingVersions )
        {
            this.existingVersions = existingVersions;
        }

        public Set<String> getExistingVersions ()
        {
            return this.existingVersions;
        }
    }

    private static final GsonBuilder BUILDER = new GsonBuilder ();

    private String repositoryUrl;

    private List<Entry> artifacts = new LinkedList<> ();

    public void setRepositoryUrl ( final String repositoryUrl )
    {
        this.repositoryUrl = repositoryUrl;
    }

    public String getRepositoryUrl ()
    {
        return this.repositoryUrl;
    }

    public void setArtifacts ( final List<Entry> artifacts )
    {
        this.artifacts = artifacts;
    }

    public List<Entry> getArtifacts ()
    {
        return this.artifacts;
    }

    public String toJson ()
    {
        return BUILDER.create ().toJson ( this );
    }

    public static AetherResult fromJson ( final String json )
    {
        return BUILDER.create ().fromJson ( json, AetherResult.class );
    }

}
