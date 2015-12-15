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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ChannelState;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;

import com.google.gson.stream.JsonWriter;

public class ChannelWriter implements AutoCloseable
{
    private final OutputStream stream;

    public ChannelWriter ( final OutputStream stream )
    {
        this.stream = stream;
    }

    @Override
    public void close () throws IOException
    {
        this.stream.close ();
    }

    public void write ( final ModifyContextImpl context ) throws IOException
    {
        final OutputStreamWriter writer = new OutputStreamWriter ( this.stream, StandardCharsets.UTF_8 );

        final JsonWriter jw = new JsonWriter ( writer );

        jw.setSerializeNulls ( true );
        jw.setIndent ( "  " );

        // begin write

        jw.beginObject ();

        final ChannelState state = context.getState ();

        jw.name ( "creationTimestamp" );
        writeTimestamp ( jw, state.getCreationTimestamp () );

        jw.name ( "modificationTimestamp" );
        writeTimestamp ( jw, state.getModificationTimestamp () );
        jw.name ( "locked" ).value ( state.isLocked () );

        {
            jw.name ( "aspects" ).beginObject ();
            jw.name ( "map" ).beginObject ();
            for ( final Map.Entry<String, String> entry : context.getAspectStates ().entrySet () )
            {
                jw.name ( entry.getKey () );
                if ( entry.getValue () != null )
                {
                    jw.value ( entry.getValue () );
                }
                else
                {
                    jw.nullValue ();
                }
            }
            jw.endObject (); // map
            jw.endObject (); // aspects
        }

        {
            jw.name ( "artifacts" ).beginObject ();

            for ( final Map.Entry<String, ArtifactInformation> entry : context.getArtifacts ().entrySet () )
            {
                jw.name ( entry.getKey () ).beginObject ();

                writeArtifact ( jw, entry.getValue () );

                jw.endObject ();
            }

            jw.endObject ();
        }

        {
            jw.name ( "cacheEntries" ).beginObject ();
            for ( final Map.Entry<MetaKey, CacheEntryInformation> entry : context.getCacheEntries ().entrySet () )
            {
                jw.name ( entry.getKey ().toString () ).beginObject ();

                jw.name ( "name" ).value ( entry.getValue ().getName () );
                jw.name ( "size" ).value ( entry.getValue ().getSize () );
                jw.name ( "mimeType" ).value ( entry.getValue ().getMimeType () );
                jw.name ( "timestamp" );
                writeTimestamp ( jw, entry.getValue ().getTimestamp () );;

                jw.endObject ();
            }
            jw.endObject ();
        }

        writeValidationMessages ( jw, context.getValidationMessages () );

        writeMetaData ( "providedMetaData", jw, context.getProvidedMetaData () );
        writeMetaData ( "extractedMetaData", jw, context.getExtractedMetaData () );

        jw.endObject (); // channel

        jw.flush ();
    }

    private void writeTimestamp ( final JsonWriter jw, final Instant timestamp ) throws IOException
    {
        if ( timestamp != null )
        {
            jw.value ( timestamp.toEpochMilli () );
        }
        else
        {
            jw.nullValue ();
        }
    }

    private void writeValidationMessages ( final JsonWriter jw, final Collection<ValidationMessage> messages ) throws IOException
    {
        if ( messages == null || messages.isEmpty () )
        {
            return;
        }

        jw.name ( "validationMessages" ).beginArray ();
        for ( final ValidationMessage msg : messages )
        {
            jw.beginObject ();
            jw.name ( "aspectId" ).value ( msg.getAspectId () );
            jw.name ( "severity" ).value ( msg.getSeverity ().toString () );
            jw.name ( "message" ).value ( msg.getMessage () );

            jw.name ( "artifactIds" ).beginArray ();
            for ( final String id : msg.getArtifactIds () )
            {
                jw.value ( id );
            }
            jw.endArray ();

            jw.endObject ();
        }
        jw.endArray ();
    }

    private void writeArtifact ( final JsonWriter jw, final ArtifactInformation art ) throws IOException
    {
        jw.name ( "name" ).value ( art.getName () );
        jw.name ( "size" ).value ( art.getSize () );
        jw.name ( "date" );
        writeTimestamp ( jw, art.getCreationInstant () );

        if ( art.getParentId () != null )
        {
            jw.name ( "parentId" ).value ( art.getParentId () );
        }

        if ( !art.getChildIds ().isEmpty () )
        {
            jw.name ( "childIds" ).beginArray ();
            for ( final String child : art.getChildIds () )
            {
                jw.value ( child );
            }
            jw.endArray ();
        }

        if ( !art.getFacets ().isEmpty () )
        {
            jw.name ( "facets" ).beginArray ();
            for ( final String facet : art.getFacets () )
            {
                jw.value ( facet );
            }
            jw.endArray ();
        }

        writeValidationMessages ( jw, art.getValidationMessages () );

        if ( art.getVirtualizerAspectId () != null )
        {
            jw.name ( "virtualizerAspectId" ).value ( art.getVirtualizerAspectId () );
        }

        writeMetaData ( "providedMetaData", jw, art.getProvidedMetaData () );
        writeMetaData ( "extractedMetaData", jw, art.getExtractedMetaData () );
    }

    private void writeMetaData ( final String name, final JsonWriter jw, final Map<MetaKey, String> metadata ) throws IOException
    {
        if ( metadata == null || metadata.isEmpty () )
        {
            return;
        }

        jw.name ( name ).beginObject ();
        for ( final Map.Entry<MetaKey, String> entry : metadata.entrySet () )
        {
            if ( entry.getValue () == null )
            {
                continue;
            }
            jw.name ( entry.getKey ().toString () ).value ( entry.getValue () );
        }
        jw.endObject ();
    }
}
