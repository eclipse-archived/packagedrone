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
package org.eclipse.packagedrone.web.tags;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.packagedrone.utils.Suppressed;
import org.eclipse.packagedrone.utils.io.IOConsumer;

import com.google.gson.stream.JsonWriter;

public final class JsonFunctions
{
    private JsonFunctions ()
    {
    }

    public static String toJson ( final Object value )
    {
        if ( value == null )
        {
            return "[]"; // quick return
        }

        if ( value instanceof Iterable<?> )
        {
            return fromIterator ( ( (Iterable<?>)value ).iterator () );
        }
        else if ( value.getClass ().isArray () )
        {
            return fromStream ( Arrays.<Object> stream ( (Object[])value ) );
        }
        else if ( value instanceof Stream<?> )
        {
            return fromStream ( (Stream<?>)value );
        }
        else if ( value instanceof Iterator<?> )
        {
            return fromIterator ( (Iterator<?>)value );
        }
        else
        {
            return from ( jw -> addValue ( jw, value ) );
        }
    }

    private static String fromStream ( final Stream<?> stream )
    {
        return from ( jw -> stream.forEachOrdered ( value -> addValue ( jw, value ) ) );
    }

    private static String fromIterator ( final Iterator<?> iter )
    {
        return from ( jw -> iter.forEachRemaining ( value -> addValue ( jw, value ) ) );
    }

    private static String from ( final Consumer<JsonWriter> operation )
    {
        try ( JsonBuilder jb = new JsonBuilder () )
        {
            jb.use ( jw -> {
                jw.beginArray ();
                operation.accept ( jw );
                jw.endArray ();
            } );

            return jb.toString ();
        }
    }

    private static class JsonBuilder implements AutoCloseable
    {
        private final StringWriter sw;

        private final JsonWriter jw;

        public JsonBuilder ()
        {
            this.sw = new StringWriter ();
            this.jw = new JsonWriter ( this.sw );
        }

        public void use ( final IOConsumer<JsonWriter> operation )
        {
            try
            {
                operation.accept ( this.jw );
            }
            catch ( final IOException e )
            {
                throw new RuntimeException ( e );
            }
        }

        @Override
        public String toString ()
        {
            return this.sw.toString ();
        }

        @Override
        public void close ()
        {
            try ( final Suppressed<RuntimeException> s = new Suppressed<> ( RuntimeException::new ) )
            {
                s.close ( this.jw );
                s.close ( this.sw );
            }
        }
    }

    private static void addValue ( final JsonWriter jw, final Object entry )
    {
        try
        {
            if ( entry != null )
            {
                jw.value ( entry.toString () );
            }
            else
            {
                jw.nullValue ();
            }
        }
        catch ( final IOException e )
        {
        }
    }
}
