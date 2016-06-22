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
package org.eclipse.packagedrone.utils.gson;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant>
{
    public static final InstantTypeAdapter DEFAULT_INSTANCE = new InstantTypeAdapter ();

    private final DateTimeFormatter formatter;

    public InstantTypeAdapter ( final DateTimeFormatter formatter )
    {
        this.formatter = formatter.withLocale ( Locale.US );
    }

    public InstantTypeAdapter ()
    {
        this ( DateTimeFormatter.ISO_INSTANT.withLocale ( Locale.US ) );
    }

    @Override
    public Instant deserialize ( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
    {
        if ( ! ( json instanceof JsonPrimitive ) )
        {
            throw new JsonParseException ( "Timestamps should be encoded as JSON strings" );
        }

        return Instant.from ( this.formatter.parse ( json.getAsString () ) );
    }

    @Override
    public JsonElement serialize ( final Instant src, final Type typeOfSrc, final JsonSerializationContext context )
    {
        return new JsonPrimitive ( this.formatter.format ( src ) );
    }

}
