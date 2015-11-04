/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

public final class HashHelper
{
    private HashHelper ()
    {
    }

    public static Map<String, HashCode> createChecksums ( final Path file, final Map<String, HashFunction> functions ) throws IOException
    {
        if ( functions.isEmpty () )
        {
            return Collections.emptyMap ();
        }

        try ( BufferedInputStream is = new BufferedInputStream ( new FileInputStream ( file.toString () ) ) )
        {
            return createChecksums ( is, functions );
        }
    }

    public static Map<String, HashCode> createChecksums ( final InputStream stream, final Map<String, HashFunction> functions ) throws IOException
    {
        if ( functions.isEmpty () )
        {
            return Collections.emptyMap ();
        }

        // init hashers

        final Map<String, Hasher> hasherMap = new HashMap<> ();
        final Hasher[] hashers = new Hasher[functions.size ()];
        int i = 0;
        for ( final Map.Entry<String, HashFunction> entry : functions.entrySet () )
        {
            hashers[i] = entry.getValue ().newHasher ();
            hasherMap.put ( entry.getKey (), hashers[i] );
            i++;
        }

        // read data

        final byte[] buffer = new byte[4096];
        int len;
        while ( ( len = stream.read ( buffer ) ) >= 0 )
        {
            for ( final Hasher hasher : hashers )
            {
                hasher.putBytes ( buffer, 0, len );
            }
        }

        // finalize hashes

        final Map<String, HashCode> result = new HashMap<String, HashCode> ( hashers.length );
        for ( final Map.Entry<String, Hasher> entry : hasherMap.entrySet () )
        {
            result.put ( entry.getKey (), entry.getValue ().hash () );
        }

        // return result

        return result;
    }
}
