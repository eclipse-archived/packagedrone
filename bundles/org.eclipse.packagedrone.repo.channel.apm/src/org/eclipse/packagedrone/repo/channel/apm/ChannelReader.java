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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.Severity;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.eclipse.packagedrone.repo.channel.apm.store.BlobStore;
import org.eclipse.packagedrone.repo.channel.apm.store.CacheStore;
import org.osgi.service.event.EventAdmin;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class ChannelReader implements AutoCloseable
{
    private final InputStream stream;

    private final String channelId;

    private final EventAdmin eventAdmin;

    private final BlobStore store;

    private final CacheStore cacheStore;

    private final DateFormat dateFormat;

    private long numberOfBytes;

    public ChannelReader ( final InputStream stream, final String channelId, final EventAdmin eventAdmin, final BlobStore store, final CacheStore cacheStore )
    {
        this.stream = stream;
        this.channelId = channelId;
        this.eventAdmin = eventAdmin;
        this.store = store;
        this.cacheStore = cacheStore;

        this.dateFormat = new SimpleDateFormat ( ChannelModelProvider.DATE_FORMAT );
    }

    @SuppressWarnings ( "resource" )
    public ModifyContextImpl read () throws IOException
    {
        this.numberOfBytes = 0;

        final Reader reader = new InputStreamReader ( this.stream, StandardCharsets.UTF_8 );

        final ChannelState.Builder state = new ChannelState.Builder ();

        Boolean locked = null;

        Map<MetaKey, CacheEntryInformation> cacheEntries = Collections.emptyMap ();
        Map<String, ArtifactInformation> artifacts = Collections.emptyMap ();
        Map<MetaKey, String> extractedMetaData = Collections.emptyMap ();
        Map<MetaKey, String> providedMetaData = Collections.emptyMap ();
        Map<String, String> aspects = new HashMap<> ();

        final JsonReader jr = new JsonReader ( reader );

        jr.beginObject ();
        while ( jr.hasNext () )
        {
            final String name = jr.nextName ();
            switch ( name )
            {
                case "description":
                    state.setDescription ( jr.nextString () );
                    break;
                case "locked":
                    state.setLocked ( locked = jr.nextBoolean () );
                    break;
                case "creationTimestamp":
                    state.setCreationTimestamp ( readTime ( jr ) );
                    break;
                case "modificationTimestamp":
                    state.setModificationTimestamp ( readTime ( jr ) );
                    break;
                case "cacheEntries":
                    cacheEntries = readCacheEntries ( jr );
                    break;
                case "artifacts":
                    artifacts = readArtifacts ( jr );
                    break;
                case "extractedMetaData":
                    extractedMetaData = readMetadata ( jr );
                    break;
                case "providedMetaData":
                    providedMetaData = readMetadata ( jr );
                    break;
                case "validationMessages":
                    state.setValidationMessages ( readValidationMessages ( jr ) );
                    break;
                case "aspects":
                    aspects = readAspects ( jr );
                    break;
                default:
                    jr.skipValue ();
                    break;
            }
        }
        jr.endObject ();

        if ( locked == null )
        {
            throw new IOException ( "Missing values for channel" );
        }

        // transient information

        state.setNumberOfArtifacts ( artifacts.size () );
        state.setNumberOfBytes ( this.numberOfBytes );

        // create result

        return new ModifyContextImpl ( this.channelId, this.eventAdmin, this.store, this.cacheStore, state.build (), aspects, artifacts, cacheEntries, extractedMetaData, providedMetaData );
    }

    private Map<String, String> readAspects ( final JsonReader jr ) throws IOException
    {
        final Map<String, String> result = new HashMap<> ();

        jr.beginObject ();
        while ( jr.hasNext () )
        {
            switch ( jr.nextName () )
            {
                case "map":
                    jr.beginObject ();
                    while ( jr.hasNext () )
                    {
                        final String id = jr.nextName ();
                        String value = null;
                        if ( jr.peek () == JsonToken.STRING )
                        {
                            value = jr.nextString ();
                        }
                        else
                        {
                            jr.skipValue ();
                        }
                        result.put ( id, value );
                    }
                    jr.endObject ();
                    break;
            }
        }
        jr.endObject ();

        return result;
    }

    private Map<String, ArtifactInformation> readArtifacts ( final JsonReader jr ) throws IOException
    {
        jr.beginObject ();

        final Map<String, ArtifactInformation> result = new HashMap<> ();

        while ( jr.hasNext () )
        {
            final String id = jr.nextName ();
            jr.beginObject ();

            String name = null;
            Long size = null;
            Instant creationTimestamp = null;
            String parentId = null;
            Set<String> childIds = Collections.emptySet ();
            Set<String> facets = Collections.emptySet ();
            String virtualizerAspectId = null;
            List<ValidationMessage> validationMessages = Collections.emptyList ();
            Map<MetaKey, String> extractedMetaData = Collections.emptyMap ();
            Map<MetaKey, String> providedMetaData = Collections.emptyMap ();

            while ( jr.hasNext () )
            {
                final String ele = jr.nextName ();
                switch ( ele )
                {
                    case "name":
                        name = jr.nextString ();
                        break;
                    case "size":
                        size = jr.nextLong ();
                        break;
                    case "date":
                        creationTimestamp = readTime ( jr );
                        break;
                    case "parentId":
                        parentId = jr.nextString ();
                        break;
                    case "childIds":
                        childIds = readSet ( jr );
                        break;
                    case "facets":
                        facets = readSet ( jr );
                        break;
                    case "virtualizerAspectId":
                        virtualizerAspectId = jr.nextString ();
                        break;
                    case "extractedMetaData":
                        extractedMetaData = readMetadata ( jr );
                        break;
                    case "providedMetaData":
                        providedMetaData = readMetadata ( jr );
                        break;
                    case "validationMessages":
                        validationMessages = readValidationMessages ( jr );
                        break;
                    default:
                        jr.skipValue ();
                        break;
                }
            }
            jr.endObject ();

            if ( id == null || name == null || size == null || creationTimestamp == null )
            {
                throw new IOException ( "Missing values for artifact" );
            }

            this.numberOfBytes += size;

            result.put ( id, new ArtifactInformation ( id, parentId, childIds, name, size, creationTimestamp, facets, validationMessages, providedMetaData, extractedMetaData, virtualizerAspectId ) );
        }

        jr.endObject ();

        return result;
    }

    private Map<MetaKey, String> readMetadata ( final JsonReader jr ) throws IOException
    {
        final Map<MetaKey, String> result = new HashMap<> ();

        jr.beginObject ();

        while ( jr.hasNext () )
        {
            final String name = jr.nextName ();
            if ( jr.peek () == JsonToken.NULL )
            {
                jr.skipValue ();
            }
            else
            {
                final String value = jr.nextString ();
                result.put ( MetaKey.fromString ( name ), value );
            }
        }

        jr.endObject ();

        return result;
    }

    private List<ValidationMessage> readValidationMessages ( final JsonReader jr ) throws IOException
    {
        final List<ValidationMessage> result = new LinkedList<> ();

        jr.beginArray ();
        while ( jr.hasNext () )
        {
            result.add ( readValidationMessage ( jr ) );
        }
        jr.endArray ();

        return result;
    }

    private ValidationMessage readValidationMessage ( final JsonReader jr ) throws IOException
    {
        String aspectId = null;
        Severity severity = null;
        String message = null;
        Set<String> artifactIds = Collections.emptySet ();

        jr.beginObject ();
        while ( jr.hasNext () )
        {
            final String name = jr.nextName ();
            switch ( name )
            {
                case "aspectId":
                    aspectId = jr.nextString ();
                    break;
                case "severity":
                    severity = Severity.valueOf ( jr.nextString () );
                    break;
                case "message":
                    message = jr.nextString ();
                    break;
                case "artifactIds":
                    artifactIds = readSet ( jr );
                    break;
            }
        }
        jr.endObject ();

        if ( aspectId == null || severity == null || message == null )
        {
            throw new IOException ( "Missing values in validation message" );
        }

        return new ValidationMessage ( aspectId, severity, message, artifactIds );
    }

    private Set<String> readSet ( final JsonReader jr ) throws IOException
    {
        final Set<String> result = new HashSet<> ();

        jr.beginArray ();
        while ( jr.hasNext () )
        {
            result.add ( jr.nextString () );
        }
        jr.endArray ();

        return result;
    }

    private Map<MetaKey, CacheEntryInformation> readCacheEntries ( final JsonReader jr ) throws IOException
    {
        final Map<MetaKey, CacheEntryInformation> result = new HashMap<> ();

        jr.beginObject ();
        while ( jr.hasNext () )
        {
            final String entryName = jr.nextName ();
            jr.beginObject ();

            String name = null;
            Long size = null;
            String mimeType = null;
            Instant timestamp = null;

            while ( jr.hasNext () )
            {
                final String ele = jr.nextName ();
                switch ( ele )
                {
                    case "name":
                        name = jr.nextString ();
                        break;
                    case "size":
                        size = jr.nextLong ();
                        break;
                    case "mimeType":
                        mimeType = jr.nextString ();
                        break;
                    case "timestamp":
                        timestamp = readTime ( jr );
                        break;
                    default:
                        jr.skipValue ();
                        break;
                }
            }

            if ( name == null || size == null || mimeType == null || timestamp == null )
            {
                throw new IOException ( "Invalid format" );
            }

            jr.endObject ();

            final MetaKey key = MetaKey.fromString ( entryName );

            result.put ( key, new CacheEntryInformation ( key, name, size, mimeType, timestamp ) );
        }
        jr.endObject ();

        return result;
    }

    private Instant readTime ( final JsonReader jr ) throws IOException
    {
        final JsonToken peek = jr.peek ();
        if ( peek == JsonToken.NUMBER )
        {
            return Instant.ofEpochMilli ( jr.nextLong () );
        }
        else if ( peek == JsonToken.NULL )
        {
            jr.nextNull ();
            return null;
        }
        else if ( peek == JsonToken.STRING )
        {
            final String str = jr.nextString ();

            try
            {
                return Instant.ofEpochMilli ( Long.parseLong ( str ) );
            }
            catch ( final NumberFormatException e )
            {
                try
                {
                    return this.dateFormat.parse ( str ).toInstant ();
                }
                catch ( final ParseException e2 )
                {
                    throw new IOException ( e2 );
                }
            }
        }
        else
        {
            throw new IOException ( String.format ( "Invalid timestamp token: %s", peek ) );
        }
    }

    @Override
    public void close () throws IOException
    {
        this.stream.close ();
    }
}
