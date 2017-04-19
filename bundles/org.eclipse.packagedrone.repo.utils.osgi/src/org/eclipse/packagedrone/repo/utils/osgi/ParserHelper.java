/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.utils.osgi;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.osgi.framework.Version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public final class ParserHelper
{
    private ParserHelper ()
    {
    }

    public static Map<String, Properties> loadLocalization ( final ZipFile file, final String loc ) throws IOException
    {
        final Map<String, Properties> locs = new HashMap<> ();

        final Pattern pattern = Pattern.compile ( Pattern.quote ( loc ) + "(|_[a-z]{2}-[A-Z]{2})\\.properties" );

        final Enumeration<? extends ZipEntry> en = file.entries ();
        while ( en.hasMoreElements () )
        {
            final ZipEntry ze = en.nextElement ();
            final Matcher m = pattern.matcher ( ze.getName () );
            if ( m.matches () )
            {
                final String locale = makeLocale ( m.group ( 1 ) );
                final Properties properties = loadProperties ( file, ze );
                locs.put ( locale, properties );
            }
        }
        return locs;
    }

    private static Properties loadProperties ( final ZipFile file, final ZipEntry ze ) throws IOException
    {
        final Properties p = new Properties ();
        p.load ( file.getInputStream ( ze ) );
        return p;
    }

    private static String makeLocale ( final String localeString )
    {
        if ( localeString.isEmpty () )
        {
            return "df_LT";
        }
        else
        {
            return localeString;
        }
    }

    private static final GsonBuilder gb;

    public static Gson newGson ()
    {
        return gb.create ();
    }

    static
    {
        gb = new GsonBuilder ();
        gb.registerTypeAdapter ( Version.class, new TypeAdapter<Version> () {

            @Override
            public Version read ( final JsonReader reader ) throws IOException
            {
                if ( reader.peek () == JsonToken.NULL )
                {
                    reader.nextNull ();
                    return null;
                }

                /* Begin compat */
                if ( reader.peek () == JsonToken.BEGIN_OBJECT )
                {
                    // old format
                    reader.beginObject ();

                    int major = 0;
                    int minor = 0;
                    int micro = 0;
                    String qualifier = null;

                    while ( reader.hasNext () )
                    {
                        final String name = reader.nextName ();
                        switch ( name )
                        {
                            case "major":
                                major = reader.nextInt ();
                                break;
                            case "minor":
                                minor = reader.nextInt ();
                                break;
                            case "micro":
                                micro = reader.nextInt ();
                                break;
                            case "qualifier":
                                qualifier = reader.peek () == JsonToken.NULL ? null : reader.nextString ();
                                break;
                            default:
                                break;
                        }
                    }
                    reader.endObject ();
                    return new Version ( major, minor, micro, qualifier );
                }
                /* end compat */

                final String str = reader.nextString ();
                if ( str == null )
                {
                    return null;
                }
                return new Version ( str );
            }

            @Override
            public void write ( final JsonWriter writer, final Version value ) throws IOException
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
        } );
    }
}
