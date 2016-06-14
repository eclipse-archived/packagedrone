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
package org.eclipse.packagedrone.repo.channel.transfer;

import static org.eclipse.packagedrone.repo.api.transfer.ContentProvider.string;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.api.transfer.TransferArchiveReader;
import org.eclipse.packagedrone.repo.api.transfer.TransferArchiveWriter;
import org.eclipse.packagedrone.repo.api.transfer.TransferWriterEntryContext;
import org.eclipse.packagedrone.repo.api.transfer.TransferArchiveReader.Handler;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;

public class TransferArchiveTest
{
    private final static Path BASE = Paths.get ( "target", "test" );

    @BeforeClass
    public static void setup () throws IOException
    {
        Files.createDirectories ( BASE );
    }

    @Test
    public void write1 () throws IOException
    {
        try ( TransferArchiveWriter writer = new TransferArchiveWriter ( Files.newOutputStream ( BASE.resolve ( "test1.zip" ) ) ) )
        {
            final TransferWriterEntryContext test1 = writer.createEntry ( "test1", make ( "foo:bar", "value" ), string ( "foo bar" ) );
            final TransferWriterEntryContext test2 = writer.createEntry ( "test2", null, string ( "foo bar 2" ) );

            test1.createEntry ( "test1a", null, string ( "test1a" ) );
            test1.createEntry ( "test1b", null, string ( "test1b" ) );

            test2.createEntry ( "test2a", null, string ( "test2a" ) );
            test2.createEntry ( "test2b", null, string ( "test2b" ) );
        }
    }

    @Test
    public void read1 () throws IOException
    {
        final List<String> ids = new LinkedList<> ();

        try ( final TransferArchiveReader reader = new TransferArchiveReader ( Files.newInputStream ( BASE.resolve ( "test1.zip" ) ) ) )
        {
            reader.process ( new Handler () {
                @Override
                public String handleEntry ( final String parentId, final String artifactName, final Map<MetaKey, String> metadata, final InputStream stream ) throws IOException
                {
                    System.out.format ( "========= %s ========= %n", artifactName );
                    for ( final Map.Entry<MetaKey, String> entry : metadata.entrySet () )
                    {
                        System.out.format ( "  '%s' -> '%s'%n", entry.getKey (), entry.getValue () );
                    }
                    System.out.format ( "---------------------- %n", artifactName );
                    ByteStreams.copy ( stream, System.out );
                    System.out.format ( "%n---------------------- %n", artifactName );

                    final String id;
                    if ( parentId != null )
                    {
                        id = parentId + "." + artifactName;
                    }
                    else
                    {
                        id = artifactName;
                    }
                    ids.add ( id );
                    return id;
                }
            } );
        }

        Assert.assertArrayEquals ( new Object[] { "test1", "test2", "test1.test1a", "test1.test1b", "test2.test2a", "test2.test2b" }, ids.toArray () );
    }

    private Map<MetaKey, String> make ( final String key, final String value )
    {
        return Collections.singletonMap ( MetaKey.fromString ( key ), value );
    }
}
