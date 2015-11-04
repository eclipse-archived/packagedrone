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
package org.eclipse.packagedrone.repo.adapter.maven;

import java.io.IOException;
import java.util.Base64;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ByteArrayAdapter extends TypeAdapter<byte[]>
{

    @Override
    public byte[] read ( final JsonReader reader ) throws IOException
    {
        if ( reader.peek () == JsonToken.NULL )
        {
            reader.nextNull ();
            return null;
        }

        final String data = reader.nextString ();

        return Base64.getDecoder ().decode ( data );
    }

    @Override
    public void write ( final JsonWriter writer, final byte[] data ) throws IOException
    {
        if ( data == null )
        {
            writer.nullValue ();
        }
        else
        {
            writer.value ( Base64.getEncoder ().encodeToString ( data ) );
        }
    }

}
