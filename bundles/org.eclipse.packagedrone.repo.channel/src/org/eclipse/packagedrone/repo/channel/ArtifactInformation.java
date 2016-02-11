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
package org.eclipse.packagedrone.repo.channel;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.utils.io.FileNames;

public class ArtifactInformation implements Comparable<ArtifactInformation>, Validated
{
    public static final Comparator<ArtifactInformation> NAME_COMPARATOR = nullsFirst ( comparing ( ArtifactInformation::getName ) );

    private final String id;

    private final String parentId;

    // original copy-on-write instance used for the manipulator to use copy-on-write
    private final Set<String> modChildIds;

    private final Set<String> childIds;

    private final String name;

    private final long size;

    private final Instant creationTimestamp;

    // original copy-on-write instance used for the manipulator to use copy-on-write
    private final Set<String> modFacets;

    private final Set<String> facets;

    // original copy-on-write instance used for the manipulator to use copy-on-write
    private final List<ValidationMessage> modMessages;

    private final List<ValidationMessage> messages;

    private final Map<MetaKey, String> providedMetaData;

    private final Map<MetaKey, String> extractedMetaData;

    private final Map<MetaKey, String> metaData;

    private final String virtualizerAspectId;

    public ArtifactInformation ( final String id, final String parentId, final Set<String> childIds, final String name, final long size, final Instant creationTimestamp, final Set<String> facets, final List<ValidationMessage> messages, final Map<MetaKey, String> providedMetaData, final Map<MetaKey, String> extractedMetaData, final String virtualizerAspectId )
    {
        this.id = id;

        this.parentId = parentId;

        if ( childIds != null )
        {
            this.modChildIds = new CopyOnWriteArraySet<> ( childIds );
            this.childIds = unmodifiableSet ( this.modChildIds );
        }
        else
        {
            this.childIds = this.modChildIds = Collections.emptySet ();
        }

        this.name = name;
        this.size = size;
        this.creationTimestamp = creationTimestamp;

        this.modFacets = new CopyOnWriteArraySet<> ( facets );
        this.facets = unmodifiableSet ( this.modFacets );

        this.modMessages = new CopyOnWriteArrayList<> ( messages );
        this.messages = unmodifiableList ( this.modMessages );

        this.providedMetaData = providedMetaData != null ? unmodifiableMap ( new HashMap<> ( providedMetaData ) ) : emptyMap ();
        this.extractedMetaData = extractedMetaData != null ? unmodifiableMap ( new HashMap<> ( extractedMetaData ) ) : emptyMap ();

        this.metaData = MetaKeys.union ( providedMetaData, extractedMetaData );

        this.virtualizerAspectId = virtualizerAspectId;
    }

    protected ArtifactInformation ( final ArtifactInformation other )
    {
        this.id = other.id;

        this.parentId = other.parentId;
        this.childIds = other.childIds;
        this.modChildIds = other.modChildIds;

        this.name = other.name;
        this.size = other.size;
        this.creationTimestamp = other.creationTimestamp;
        this.facets = other.facets;
        this.modFacets = other.modFacets;

        this.messages = other.messages;
        this.modMessages = other.modMessages;

        this.providedMetaData = other.providedMetaData;
        this.extractedMetaData = other.extractedMetaData;

        this.metaData = other.metaData;
        this.virtualizerAspectId = other.virtualizerAspectId;
    }

    public boolean is ( final String type )
    {
        return this.facets.contains ( type );
    }

    public Set<String> getFacets ()
    {
        return this.facets;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getParentId ()
    {
        return this.parentId;
    }

    public Set<String> getChildIds ()
    {
        return this.childIds;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getBasename ()
    {
        return FileNames.getBasename ( this.name );
    }

    public long getSize ()
    {
        return this.size;
    }

    public Instant getCreationInstant ()
    {
        return this.creationTimestamp;
    }

    public Date getCreationTimestamp ()
    {
        return new Date ( this.creationTimestamp.toEpochMilli () );
    }

    public Map<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    public Map<MetaKey, String> getExtractedMetaData ()
    {
        return this.extractedMetaData;
    }

    public Map<MetaKey, String> getProvidedMetaData ()
    {
        return this.providedMetaData;
    }

    public String getVirtualizerAspectId ()
    {
        return this.virtualizerAspectId;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( ! ( obj instanceof ArtifactInformation ) )
        {
            return false;
        }
        final ArtifactInformation other = (ArtifactInformation)obj;
        if ( this.id == null )
        {
            if ( other.id != null )
            {
                return false;
            }
        }
        else if ( !this.id.equals ( other.id ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo ( final ArtifactInformation o )
    {
        return this.id.compareTo ( o.id );
    }

    @Override
    public Collection<ValidationMessage> getValidationMessages ()
    {
        return this.messages;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[%s = %s]", this.id, this.name );
    }

    public Manipulator createManipulator ()
    {
        return new Manipulator ( this );
    }

    public static class Manipulator
    {
        private final ArtifactInformation original;

        private Set<String> childIds;

        private Map<MetaKey, String> providedMetaData;

        private Map<MetaKey, String> extractedMetaData;

        private List<ValidationMessage> validationMessages;

        public Manipulator ( final ArtifactInformation original )
        {
            this.original = original;

            /* although this is a modifiable version, we _must not_ change the list content!
             * Since it is the base of an immutable object. But if we have access to the original
             * CopyOnWriteArraySet, the copy constructor can make use of the copy on write variant */
            this.childIds = new CopyOnWriteArraySet<> ( original.modChildIds );

            this.providedMetaData = new HashMap<> ( original.providedMetaData );
            this.extractedMetaData = new HashMap<> ( original.extractedMetaData );
            this.validationMessages = new CopyOnWriteArrayList<> ( original.modMessages );
        }

        public void setChildIds ( final Set<String> childIds )
        {
            this.childIds = childIds;
        }

        public Set<String> getChildIds ()
        {
            return this.childIds;
        }

        public ArtifactInformation build ()
        {
            return new ArtifactInformation ( this.original.id, this.original.parentId, this.childIds, this.original.name, this.original.size, this.original.creationTimestamp, this.original.modFacets, this.validationMessages, this.providedMetaData, this.extractedMetaData, this.original.virtualizerAspectId );
        }

        public Map<MetaKey, String> getProvidedMetaData ()
        {
            return this.providedMetaData;
        }

        public void setProvidedMetaData ( final Map<MetaKey, String> providedMetaData )
        {
            this.providedMetaData = providedMetaData;
        }

        public Map<MetaKey, String> getExtractedMetaData ()
        {
            return this.extractedMetaData;
        }

        public void setExtractedMetaData ( final Map<MetaKey, String> extractedMetaData )
        {
            this.extractedMetaData = extractedMetaData;
        }

        public void setValidationMessages ( final List<ValidationMessage> validationMessages )
        {
            this.validationMessages = validationMessages;
        }
    }
}
