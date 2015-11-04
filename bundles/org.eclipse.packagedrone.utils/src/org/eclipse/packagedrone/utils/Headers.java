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
package org.eclipse.packagedrone.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class Headers
{
    private Headers ()
    {
    }

    public static List<AttributedValue> parseList ( final String string )
    {
        if ( string == null )
        {
            return null;
        }

        final List<AttributedValue> result = new LinkedList<> ();

        for ( final String tok : split ( string, ',' ) )
        {
            result.add ( parse ( tok ) );
        }

        return result;
    }

    public static <T extends Collection<String>> T parseStringCollection ( final String string, final Supplier<T> provider )
    {
        final List<AttributedValue> result = parseList ( string );
        if ( result == null )
        {
            return null;
        }

        final T col = provider.get ();

        for ( final AttributedValue av : result )
        {
            col.add ( av.getValue () );
        }

        return col;
    }

    public static List<String> parseStringList ( final String string )
    {
        return parseStringCollection ( string, LinkedList::new );
    }

    public static Set<String> parseStringSet ( final String string )
    {
        return parseStringCollection ( string, HashSet::new );
    }

    public static AttributedValue parse ( final String string )
    {
        if ( string == null )
        {
            return null;
        }

        final String[] toks = segments ( string );

        if ( toks.length == 1 )
        {
            return new AttributedValue ( toks[0] );
        }
        else
        {
            return new AttributedValue ( toks[0], splitAttributes ( 1, toks ) );
        }
    }

    public static Map<String, String> splitAttributes ( final int skipToks, final String[] toks )
    {
        final Map<String, String> result = new HashMap<> ();

        for ( int i = skipToks; i < toks.length; i++ )
        {
            parseToken ( toks[i], result );
        }

        return result;
    }

    private static void parseToken ( final String v, final Map<String, String> result )
    {
        final StringBuilder key = new StringBuilder ();
        StringBuilder value = null;

        Character quote = null;

        int pos = 0;
        while ( pos < v.length () )
        {
            final char c = v.charAt ( pos );
            final Character next = pos + 1 < v.length () ? v.charAt ( pos + 1 ) : null;

            if ( quote == null )
            {
                if ( c == '"' )
                {
                    quote = c;
                }
                else if ( value != null )
                {
                    value.append ( c );
                }
                else if ( c == ':' && next != null && next.charValue () == '=' )
                {
                    value = new StringBuilder ();
                    pos++; // skip one char
                }
                else if ( c == '=' )
                {
                    value = new StringBuilder ();
                }
                else
                {
                    key.append ( c );
                }
            }
            else
            {
                if ( c == quote )
                {
                    quote = null;
                }
                else if ( value != null )
                {
                    value.append ( c );
                }
                else
                {
                    key.append ( c );
                }

            }
            pos++;
        }

        // return result

        if ( key.length () <= 0 )
        {
            return;
        }

        if ( value != null )
        {
            result.put ( key.toString (), value.toString () );
        }
        else
        {
            final String kv = key.toString ();
            result.put ( kv, kv );
        }
    }

    public static String[] segments ( final String string )
    {
        return split ( string, ';' );
    }

    public static String[] split ( final String string, final char delimiter )
    {
        final List<String> result = new LinkedList<String> ();

        int pos = 0;

        Character quote = null;
        StringBuilder sb = new StringBuilder ();
        while ( pos < string.length () )
        {
            final char c = string.charAt ( pos );
            if ( quote == null )
            {
                if ( c == '"' )
                {
                    quote = c;
                    sb.append ( c );
                }
                else if ( Character.isWhitespace ( c ) )
                {
                    // ignore
                }
                else if ( c == delimiter )
                {
                    result.add ( sb.toString () );
                    sb = new StringBuilder ();
                }
                else
                {
                    sb.append ( c );
                }
            }
            else
            {
                // quoted
                if ( c == quote )
                {
                    quote = null;
                }
                sb.append ( c );
            }

            pos++;
        }

        if ( sb.length () > 0 )
        {
            result.add ( sb.toString () );
        }

        return result.toArray ( new String[result.size ()] );
    }

}
