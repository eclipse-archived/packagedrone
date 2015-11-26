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

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.apm.aspect.AspectMapModel;

public class ChannelModel
{
    private String description;

    private boolean locked;

    private Map<MetaKey, String> providedMetaData;

    private Map<MetaKey, String> extractedMetaData;

    private final Map<String, ArtifactModel> artifacts;

    private final Map<MetaKey, CacheEntryModel> cacheEntries;

    private final AspectMapModel aspects;

    private List<ValidationMessageModel> validationMessages;

    private Date creationTimestamp;

    private Date modificationTimestamp;

    public ChannelModel ()
    {
        this.providedMetaData = new HashMap<> ();
        this.artifacts = new HashMap<> ();
        this.cacheEntries = new HashMap<> ();

        this.aspects = new AspectMapModel ();

        this.validationMessages = new ArrayList<> ();
    }

    public ChannelModel ( final ChannelModel other )
    {
        this.creationTimestamp = other.creationTimestamp;
        this.modificationTimestamp = other.modificationTimestamp;

        this.description = other.description;

        this.locked = other.locked;

        this.extractedMetaData = new HashMap<> ( other.extractedMetaData );
        this.providedMetaData = new HashMap<> ( other.providedMetaData );

        // copy by ctor

        this.artifacts = other.artifacts.entrySet ().stream ().collect ( toMap ( Entry::getKey, entry -> new ArtifactModel ( entry.getValue () ) ) );
        this.cacheEntries = other.cacheEntries.entrySet ().stream ().collect ( toMap ( Entry::getKey, entry -> new CacheEntryModel ( entry.getValue () ) ) );

        this.aspects = new AspectMapModel ( other.aspects );

        this.validationMessages = other.validationMessages != null ? new ArrayList<> ( other.validationMessages ) : new ArrayList<> ();
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setProvidedMetaData ( final Map<MetaKey, String> providedMetaData )
    {
        this.providedMetaData = providedMetaData;
    }

    public Map<MetaKey, String> getProvidedMetaData ()
    {
        return this.providedMetaData;
    }

    public void setExtractedMetaData ( final Map<MetaKey, String> extractedMetaData )
    {
        this.extractedMetaData = extractedMetaData;
    }

    public Map<MetaKey, String> getExtractedMetaData ()
    {
        return this.extractedMetaData;
    }

    public void setLocked ( final boolean locked )
    {
        this.locked = locked;
    }

    public boolean isLocked ()
    {
        return this.locked;
    }

    public void addArtifact ( final ArtifactInformation ai )
    {
        this.artifacts.put ( ai.getId (), ArtifactModel.fromInformation ( ai ) );
    }

    public void removeArtifact ( final String artifactId )
    {
        this.artifacts.remove ( artifactId );
    }

    public Map<String, ArtifactModel> getArtifacts ()
    {
        return Collections.unmodifiableMap ( this.artifacts );
    }

    public AspectMapModel getAspects ()
    {
        return this.aspects;
    }

    public Map<MetaKey, CacheEntryModel> getCacheEntries ()
    {
        return this.cacheEntries;
    }

    public void setValidationMessages ( final List<ValidationMessageModel> validationMessages )
    {
        this.validationMessages = validationMessages;
    }

    public List<ValidationMessageModel> getValidationMessages ()
    {
        return this.validationMessages;
    }

    public void setCreationTimestamp ( final Date creationTimestamp )
    {
        this.creationTimestamp = creationTimestamp;
    }

    public Date getCreationTimestamp ()
    {
        return this.creationTimestamp;
    }

    public void setModificationTimestamp ( final Date modificationTimestamp )
    {
        this.modificationTimestamp = modificationTimestamp;
    }

    public Date getModificationTimestamp ()
    {
        return this.modificationTimestamp;
    }

}
