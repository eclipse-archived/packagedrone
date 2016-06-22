/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.tags;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.utils.Strings;
import org.eclipse.packagedrone.web.util.Requests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Functions
{
    private static final Comparator<Object> COMPARATOR = new AnyComparator ();

    private static class AnyComparator implements Comparator<Object>
    {
        @SuppressWarnings ( { "rawtypes", "unchecked" } )
        @Override
        public int compare ( final Object o1, final Object o2 )
        {
            // maybe we are lucky
            if ( o1 == o2 )
            {
                return 0;
            }

            /// check of we can delegate that
            if ( o1 instanceof Comparable && o2 instanceof Comparable )
            {
                return ( (Comparable)o1 ).compareTo ( o2 );
            }

            if ( o1 == null )
            {
                return -1; // the other one is not null, we already checked that
            }

            if ( o2 == null )
            {
                return 1;
            }

            // compare by string
            return o1.toString ().compareTo ( o2.toString () );
        }
    }

    private static final MessageDigest MD;

    static
    {
        MessageDigest md = null;
        try
        {
            md = MessageDigest.getInstance ( "MD5" );
        }
        catch ( final NoSuchAlgorithmException e )
        {
        }
        MD = md;
    };

    public static String active ( final HttpServletRequest request, final String targetUrl )
    {
        if ( targetUrl == null )
        {
            return "";
        }

        return Requests.getOriginalPath ( request ).equals ( targetUrl ) ? "active" : "";
    }

    public static String toFirstUpper ( final String string )
    {
        if ( string == null || string.isEmpty () )
        {
            return string;
        }

        return string.substring ( 0, 1 ).toUpperCase () + string.substring ( 1 );
    }

    public static String gravatar ( final String email )
    {
        if ( email == null || MD == null )
        {
            return null;
        }

        if ( email.isEmpty () )
        {
            return null;
        }

        try
        {
            return Strings.hex ( MD.digest ( email.getBytes ( "CP1252" ) ) );
        }
        catch ( final UnsupportedEncodingException e )
        {
            return null;
        }
    }

    public static String json ( final Object object )
    {
        final Gson gson = new GsonBuilder ().create ();
        return gson.toJson ( object );
    }

    public static List<?> sort ( final Collection<?> items )
    {
        if ( items == null )
        {
            return null;
        }

        if ( items.isEmpty () )
        {
            return Collections.emptyList ();
        }

        final List<?> result = new ArrayList<> ( items );
        Collections.sort ( result, COMPARATOR );
        return result;
    }

    public static String encode ( final String str )
    {
        try
        {
            return URLEncoder.encode ( str, "UTF-8" );
        }
        catch ( final UnsupportedEncodingException e )
        {
            throw new IllegalStateException ( e );
        }
    }

    public static Date toDate ( final Instant instant )
    {
        return Date.from ( instant );
    }

    public static String limit ( final String value, final int length, String ellipsis )
    {
        if ( value == null || value.length () < length )
        {
            return value;
        }

        if ( length <= 0 )
        {
            return "";
        }

        if ( ellipsis == null )
        {
            ellipsis = "…";
        }

        return value.substring ( 0, length ) + ellipsis;
    }
}
