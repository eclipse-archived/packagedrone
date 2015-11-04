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
package org.eclipse.packagedrone.utils.deb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.eclipse.packagedrone.utils.Strings;
import org.eclipse.packagedrone.utils.deb.internal.BinarySectionPackagesFile;

public final class Packages
{
    private Packages ()
    {
    }

    public static Map<String, String> parseControlFile ( final File packageFile ) throws IOException, ParserException
    {
        try ( final ArArchiveInputStream in = new ArArchiveInputStream ( new FileInputStream ( packageFile ) ) )
        {
            ArchiveEntry ar;
            while ( ( ar = in.getNextEntry () ) != null )
            {
                if ( !ar.getName ().equals ( "control.tar.gz" ) )
                {
                    continue;
                }
                try ( final TarArchiveInputStream inputStream = new TarArchiveInputStream ( new GZIPInputStream ( in ) ) )
                {
                    TarArchiveEntry te;
                    while ( ( te = inputStream.getNextTarEntry () ) != null )
                    {
                        String name = te.getName ();
                        if ( name.startsWith ( "./" ) )
                        {
                            name = name.substring ( 2 );
                        }
                        if ( !name.equals ( "control" ) )
                        {
                            continue;
                        }
                        return parseControlFile ( inputStream );
                    }
                }
            }
        }
        return null;
    }

    public static Map<String, String> parseControlFile ( final InputStream inputStream ) throws IOException, ParserException
    {
        return ControlFileParser.parse ( inputStream );
    }

    public static List<Map<String, String>> parseStatusFile ( final InputStream inputStream ) throws IOException, ParserException
    {
        return ControlFileParser.parseMulti ( inputStream );
    }

    public static void writeBinaryPackageValues ( final PrintWriter writer, final Map<String, String> values ) throws IOException
    {
        new ControlFileWriter ( writer, BinarySectionPackagesFile.FORMATTERS ).writeEntries ( values );
    }

    private static MessageDigest MD5;

    static
    {
        try
        {
            MD5 = MessageDigest.getInstance ( "MD5" );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new RuntimeException ( e );
        }
    }

    public static String makeDescriptionMd5 ( final String string )
    {
        if ( string == null )
        {
            return null;
        }

        final StringBuilder sb = new StringBuilder ();
        try
        {
            FieldFormatter.MULTI.appendValue ( string, sb );
            sb.append ( '\n' );
        }
        catch ( final IOException e )
        {
            // this will never ever happen
        }
        final String result = sb.toString ();

        final byte[] data = MD5.digest ( result.getBytes ( StandardCharsets.UTF_8 ) );
        return Strings.hex ( data );
    }
}
