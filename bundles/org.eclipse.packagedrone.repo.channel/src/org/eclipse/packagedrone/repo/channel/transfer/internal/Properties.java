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
package org.eclipse.packagedrone.repo.channel.transfer.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public final class Properties
{
    private Properties ()
    {
    }

    public static void write ( final Map<MetaKey, String> properties, final Writer output ) throws IOException
    {
        @SuppressWarnings ( "resource" )
        final JsonWriter writer = new JsonWriter ( output );

        writer.beginObject ();

        for ( final Map.Entry<MetaKey, String> entry : properties.entrySet () )
        {
            writer.name ( entry.getKey ().toString () );
            if ( entry.getValue () != null )
            {
                writer.value ( entry.getValue () );
            }
        }

        writer.endObject ();

        writer.flush ();
    }

    public static Map<MetaKey, String> read ( final Reader input ) throws IOException
    {
        @SuppressWarnings ( "resource" )
        final JsonReader reader = new JsonReader ( input );

        final Map<MetaKey, String> result = new HashMap<> ();

        reader.beginObject ();

        while ( reader.hasNext () )
        {
            final String name = reader.nextName ();
            if ( reader.peek () == JsonToken.NULL )
            {
                reader.nextNull ();
            }
            else
            {
                result.put ( MetaKey.fromString ( name ), reader.nextString () );
            }
        }

        reader.endObject ();

        return result;
    }
}
