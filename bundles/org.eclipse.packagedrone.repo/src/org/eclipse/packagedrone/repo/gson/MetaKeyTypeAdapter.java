/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.gson;

import java.io.IOException;

import org.eclipse.packagedrone.repo.MetaKey;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class MetaKeyTypeAdapter extends TypeAdapter<MetaKey>
{

    public static final MetaKeyTypeAdapter INSTANCE = new MetaKeyTypeAdapter ();

    /**
     * @deprecated Use the field {@link #INSTANCE} instead
     */
    @Deprecated
    public MetaKeyTypeAdapter ()
    {
    }

    @Override
    public MetaKey read ( final JsonReader reader ) throws IOException
    {
        if ( reader.peek () == JsonToken.NULL )
        {
            return null;
        }
        else
        {
            return MetaKey.fromString ( reader.nextString () );
        }
    }

    @Override
    public void write ( final JsonWriter writer, final MetaKey value ) throws IOException
    {
        if ( value == null )
        {
            writer.nullValue ();
        }
        else
        {
            writer.value ( value.toString () );
        }
    }

}
