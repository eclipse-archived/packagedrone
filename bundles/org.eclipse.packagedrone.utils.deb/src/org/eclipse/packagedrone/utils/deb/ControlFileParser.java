/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S1943
 *******************************************************************************/
package org.eclipse.packagedrone.utils.deb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ControlFileParser
{
    private ControlFileParser ()
    {
    }

    public static List<Map<String, String>> parseMulti ( final InputStream stream ) throws IOException, ParserException
    {
        return parseMulti ( new InputStreamReader ( stream, StandardCharsets.UTF_8 ) );
    }

    public static List<Map<String, String>> parseMulti ( final Reader inputReader ) throws IOException, ParserException
    {
        LinkedHashMap<String, String> entry;

        final List<Map<String, String>> result = new LinkedList<> ();

        final BufferedReader reader = new BufferedReader ( inputReader );

        while ( ( entry = ControlFileParser.parseInternal ( reader ) ) != null )
        {
            result.add ( entry );
        }

        return result;
    }

    public static LinkedHashMap<String, String> parse ( final InputStream stream ) throws IOException, ParserException
    {
        return parse ( new InputStreamReader ( stream, StandardCharsets.UTF_8 ) );
    }

    public static LinkedHashMap<String, String> parse ( final Reader inputReader ) throws IOException, ParserException
    {
        return parseInternal ( new BufferedReader ( inputReader ) );
    }

    private static LinkedHashMap<String, String> parseInternal ( final BufferedReader reader ) throws IOException, ParserException
    {
        String line;

        final LinkedHashMap<String, String> result = new LinkedHashMap<> ();

        String currentKey = null;
        StringBuilder currentValue = new StringBuilder ();

        while ( ( line = reader.readLine () ) != null )
        {
            if ( line.isEmpty () )
            {
                break; // break - could a multi file
            }

            if ( line.startsWith ( "#" ) )
            {
                // comment line
                continue;
            }

            if ( !line.startsWith ( " " ) && !line.startsWith ( "\t" ) )
            {
                final int idx = line.indexOf ( ':' );
                if ( idx > 0 )
                {
                    // flush current key

                    if ( currentKey != null )
                    {
                        result.put ( currentKey, currentValue.toString () );
                        currentValue = new StringBuilder ();
                    }

                    // start next key

                    currentKey = line.substring ( 0, idx );
                    String val = line.substring ( idx + 1, line.length () );
                    if ( val.startsWith ( " " ) )
                    {
                        val = val.substring ( 1 );
                    }
                    currentValue.append ( val );

                }
                else
                {
                    throw new ParserException ( String.format ( "Missing ':' field delimiter in line: '%s'", line ) );
                }
            }
            else
            {
                // multiline
                currentValue.append ( '\n' );
                line = line.substring ( 1 ); // remove leading whitespace

                if ( !line.equals ( "." ) )
                {
                    // empty line
                    currentValue.append ( line );
                }
            }
        }

        if ( line == null && result.isEmpty () )
        {
            // empty file or part
            return null;
        }

        if ( currentKey != null )
        {
            result.put ( currentKey, currentValue.toString () );
        }

        return result;
    }

}
